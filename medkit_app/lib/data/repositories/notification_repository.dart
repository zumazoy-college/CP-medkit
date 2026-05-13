import '../models/notification_model.dart';
import '../services/api_service.dart';

class NotificationRepository {
  final ApiService _apiService;

  NotificationRepository(this._apiService);

  Future<List<NotificationModel>> getUnreadNotifications() async {
    print('NotificationRepository: Fetching unread notifications');
    final response = await _apiService.get('/notifications/unread');
    final list = response.data as List;
    print('NotificationRepository: Got ${list.length} unread notifications');
    return list.map((json) => NotificationModel.fromJson(json)).toList();
  }

  Future<List<NotificationModel>> getAllNotifications({
    int page = 0,
    int size = 20,
  }) async {
    print('NotificationRepository: Fetching all notifications, page=$page, size=$size');
    final response = await _apiService.get(
      '/notifications',
      queryParameters: {
        'page': page,
        'size': size,
      },
    );

    print('NotificationRepository: Response data type: ${response.data.runtimeType}');
    print('NotificationRepository: Response data: ${response.data}');

    final content = response.data['content'] as List;
    print('NotificationRepository: Got ${content.length} notifications from content');
    return content.map((json) => NotificationModel.fromJson(json)).toList();
  }

  Future<void> markAsRead(int notificationId) async {
    await _apiService.put('/notifications/$notificationId/read');
  }

  Future<void> markAllAsRead() async {
    await _apiService.put('/notifications/read-all');
  }
}
