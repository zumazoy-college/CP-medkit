import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/notification_provider.dart';
import '../../providers/appointment_provider.dart';
import '../../../core/utils/date_formatter.dart';
import '../review/create_review_screen.dart';

class AllNotificationsScreen extends StatefulWidget {
  const AllNotificationsScreen({super.key});

  @override
  State<AllNotificationsScreen> createState() => _AllNotificationsScreenState();
}

class _AllNotificationsScreenState extends State<AllNotificationsScreen> {
  final ScrollController _scrollController = ScrollController();
  int _currentPage = 0;
  bool _isLoadingMore = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<NotificationProvider>().loadAllNotifications();
    });
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent * 0.9) {
      _loadMore();
    }
  }

  Future<void> _loadMore() async {
    if (_isLoadingMore) return;

    setState(() {
      _isLoadingMore = true;
    });

    _currentPage++;
    await context.read<NotificationProvider>().loadAllNotifications(page: _currentPage);

    setState(() {
      _isLoadingMore = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Consumer<NotificationProvider>(
          builder: (context, notificationProvider, child) {
            final unreadCount = notificationProvider.notifications
                .where((n) => !n.isRead)
                .length;

            if (unreadCount > 0) {
              return Text('Уведомления ($unreadCount)');
            }
            return const Text('Уведомления');
          },
        ),
        actions: [
          Consumer<NotificationProvider>(
            builder: (context, notificationProvider, child) {
              final unreadCount = notificationProvider.notifications
                  .where((n) => !n.isRead)
                  .length;

              if (unreadCount > 0) {
                return TextButton(
                  onPressed: () async {
                    await notificationProvider.markAllAsRead();
                    if (mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Все уведомления отмечены как прочитанные')),
                      );
                    }
                  },
                  child: const Text('Прочитать все'),
                );
              }
              return const SizedBox.shrink();
            },
          ),
        ],
      ),
      body: Consumer<NotificationProvider>(
        builder: (context, notificationProvider, child) {
          if (notificationProvider.isLoading && _currentPage == 0) {
            return const Center(child: CircularProgressIndicator());
          }

          if (notificationProvider.error != null && _currentPage == 0) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(notificationProvider.error!),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      _currentPage = 0;
                      notificationProvider.loadAllNotifications();
                    },
                    child: const Text('Повторить'),
                  ),
                ],
              ),
            );
          }

          if (notificationProvider.notifications.isEmpty) {
            return const Center(
              child: Text('Нет уведомлений'),
            );
          }

          return RefreshIndicator(
            onRefresh: () async {
              _currentPage = 0;
              await notificationProvider.loadAllNotifications();
            },
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: notificationProvider.notifications.length + (_isLoadingMore ? 1 : 0),
              itemBuilder: (context, index) {
                if (index == notificationProvider.notifications.length) {
                  return const Center(
                    child: Padding(
                      padding: EdgeInsets.all(16.0),
                      child: CircularProgressIndicator(),
                    ),
                  );
                }

                final notification = notificationProvider.notifications[index];
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  color: notification.isRead ? null : Colors.blue.withValues(alpha: 0.1),
                  child: InkWell(
                    onTap: () async {
                      // Capture context before async operations
                      final navigator = Navigator.of(context);
                      final appointmentProvider = context.read<AppointmentProvider>();
                      final scaffoldMessenger = ScaffoldMessenger.of(context);

                      if (!notification.isRead) {
                        await notificationProvider.markAsRead(notification.idNotification);
                      }

                      // Navigate to CreateReviewScreen if it's a completed appointment notification
                      if (notification.type == 'appointment_completed' &&
                          notification.link != null &&
                          mounted) {
                        final appointmentId = int.tryParse(notification.link!);
                        print('Notification tapped: appointmentId=$appointmentId');

                        if (appointmentId != null) {
                          // Ensure appointments are loaded
                          if (appointmentProvider.myAppointments.isEmpty) {
                            await appointmentProvider.loadMyAppointments();
                          }

                          // Try to find appointment in existing list
                          final appointment = appointmentProvider.myAppointments
                              .where((a) => a.idAppointment == appointmentId)
                              .firstOrNull;

                          print('Found appointment: ${appointment?.doctorName}');

                          if (appointment != null) {
                            navigator.push(
                              MaterialPageRoute(
                                builder: (_) => CreateReviewScreen(
                                  appointmentId: appointmentId,
                                  doctorName: appointment.doctorName,
                                  appointmentDate: appointment.slotDate,
                                ),
                              ),
                            );
                          } else {
                            scaffoldMessenger.showSnackBar(
                              const SnackBar(content: Text('Прием не найден')),
                            );
                          }
                        }
                      }
                    },
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Icon(
                            _getNotificationIcon(notification.type),
                            size: 24,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Expanded(
                                      child: Text(
                                        notification.title,
                                        style: TextStyle(
                                          fontWeight: notification.isRead
                                              ? FontWeight.normal
                                              : FontWeight.bold,
                                          fontSize: 16,
                                        ),
                                      ),
                                    ),
                                    if (!notification.isRead)
                                      Container(
                                        width: 8,
                                        height: 8,
                                        decoration: const BoxDecoration(
                                          color: Colors.blue,
                                          shape: BoxShape.circle,
                                        ),
                                      ),
                                  ],
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  notification.message,
                                  style: Theme.of(context).textTheme.bodyMedium,
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  DateFormatter.formatDateTime(notification.createdAt),
                                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                        color: Colors.grey,
                                      ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }

  IconData _getNotificationIcon(String type) {
    switch (type.toLowerCase()) {
      case 'appointment_cancelled':
        return Icons.cancel;
      case 'appointment_reminder':
        return Icons.alarm;
      case 'appointment_booked':
        return Icons.event_available;
      case 'appointment_completed':
        return Icons.check_circle;
      case 'analysis_ready':
        return Icons.science;
      case 'new_referral':
        return Icons.assignment;
      default:
        return Icons.info;
    }
  }
}
