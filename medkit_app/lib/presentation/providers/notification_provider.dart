import 'package:flutter/foundation.dart';
import '../../data/models/notification_model.dart';
import '../../data/repositories/notification_repository.dart';

class NotificationProvider with ChangeNotifier {
  final NotificationRepository _notificationRepository;

  NotificationProvider(this._notificationRepository);

  List<NotificationModel> _notifications = [];
  List<NotificationModel> _unreadNotifications = [];
  bool _isLoading = false;
  String? _error;
  bool _hasMore = true;

  List<NotificationModel> get notifications => _notifications;
  List<NotificationModel> get unreadNotifications => _unreadNotifications;
  int get unreadCount => _unreadNotifications.length;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get hasMore => _hasMore;

  Future<void> loadUnreadNotifications() async {
    try {
      _unreadNotifications =
          await _notificationRepository.getUnreadNotifications();
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> loadAllNotifications({int page = 0, int size = 10}) async {
    print('NotificationProvider: Loading notifications, page=$page, size=$size');
    if (page == 0) {
      _isLoading = true;
      _error = null;
      _notifications = [];
      _hasMore = true;
    }
    notifyListeners();

    try {
      final newNotifications = await _notificationRepository.getAllNotifications(
        page: page,
        size: size,
      );

      print('NotificationProvider: Loaded ${newNotifications.length} notifications');

      if (page == 0) {
        _notifications = newNotifications;
      } else {
        _notifications.addAll(newNotifications);
      }

      _hasMore = newNotifications.length >= size;
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      print('NotificationProvider: Error loading notifications: $e');
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> markAsRead(int notificationId) async {
    try {
      await _notificationRepository.markAsRead(notificationId);

      _notifications = _notifications.map((n) {
        if (n.idNotification == notificationId) {
          return NotificationModel(
            idNotification: n.idNotification,
            type: n.type,
            title: n.title,
            message: n.message,
            isRead: true,
            link: n.link,
            createdAt: n.createdAt,
          );
        }
        return n;
      }).toList();

      _unreadNotifications
          .removeWhere((n) => n.idNotification == notificationId);

      notifyListeners();
    } catch (e) {
      print('NotificationProvider: Error marking as read: $e');
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> markAllAsRead() async {
    try {
      await _notificationRepository.markAllAsRead();

      _notifications = _notifications.map((n) {
        return NotificationModel(
          idNotification: n.idNotification,
          type: n.type,
          title: n.title,
          message: n.message,
          isRead: true,
          link: n.link,
          createdAt: n.createdAt,
        );
      }).toList();

      _unreadNotifications.clear();

      notifyListeners();
    } catch (e) {
      print('NotificationProvider: Error marking all as read: $e');
      _error = e.toString();
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
