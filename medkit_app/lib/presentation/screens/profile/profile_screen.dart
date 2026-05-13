import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../providers/patient_provider.dart';
import '../../providers/auth_provider.dart';
import '../../providers/appointment_provider.dart';
import '../../providers/favorite_provider.dart';
import '../../providers/notification_provider.dart';
import '../../../core/utils/date_formatter.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/constants/app_constants.dart';
import '../auth/login_screen.dart';
import '../favorites/favorites_screen.dart';
import '../settings/settings_screen.dart';
import '../notifications/all_notifications_screen.dart';
import '../review/create_review_screen.dart';

class ProfileScreen extends StatefulWidget {
  final void Function(int)? onNavigateToTab;

  const ProfileScreen({super.key, this.onNavigateToTab});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> with WidgetsBindingObserver {

  String? _getAvatarUrl(String? avatarPath) {
    if (avatarPath == null || avatarPath.isEmpty) return null;
    if (avatarPath.startsWith('http')) return avatarPath;

    final baseUrl = ApiConstants.baseUrl.replaceAll('/api', '');
    return '$baseUrl$avatarPath';
  }

  Future<Map<String, String>> _getHeaders() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString(AppConstants.tokenKey);
    return {
      'Authorization': 'Bearer ${token ?? ''}',
    };
  }
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<PatientProvider>().loadMyProfile();
      context.read<AppointmentProvider>().loadMyAppointments();
      context.read<FavoriteProvider>().loadFavorites();
      context.read<NotificationProvider>().loadAllNotifications(size: 3);
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if (state == AppLifecycleState.resumed) {
      // Refresh data when app comes back to foreground
      context.read<PatientProvider>().loadMyProfile();
      context.read<AppointmentProvider>().loadMyAppointments();
      context.read<FavoriteProvider>().loadFavorites();
      context.read<NotificationProvider>().loadAllNotifications(size: 3);
    }
  }

  Future<void> _logout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Выход'),
        content: const Text('Вы уверены, что хотите выйти?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Отмена'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Выйти'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      await context.read<AuthProvider>().logout();
      if (mounted) {
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const LoginScreen()),
          (route) => false,
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Профиль'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const SettingsScreen(),
                ),
              );
            },
          ),
        ],
      ),
      body: Consumer<PatientProvider>(
        builder: (context, patientProvider, child) {
          final patient = patientProvider.currentPatient;

          return RefreshIndicator(
            onRefresh: () async {
              await Future.wait([
                patientProvider.loadMyProfile(),
                context.read<AppointmentProvider>().loadMyAppointments(),
                context.read<FavoriteProvider>().loadFavorites(),
                context.read<NotificationProvider>().loadAllNotifications(size: 3),
              ]);
            },
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  if (patientProvider.isLoading && patient == null)
                    const Center(child: CircularProgressIndicator())
                  else if (patientProvider.error != null && patient == null)
                    Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(patientProvider.error!),
                          const SizedBox(height: 16),
                          ElevatedButton(
                            onPressed: () => patientProvider.loadMyProfile(),
                            child: const Text('Повторить'),
                          ),
                        ],
                      ),
                    )
                  else if (patient != null) ...[
                    FutureBuilder<Map<String, String>>(
                      future: _getHeaders(),
                      builder: (context, snapshot) {
                        final avatarUrl = _getAvatarUrl(patient.avatarUrl);

                        if (avatarUrl != null && snapshot.hasData) {
                          return CircleAvatar(
                            radius: 50,
                            backgroundImage: CachedNetworkImageProvider(
                              avatarUrl,
                              headers: snapshot.data,
                            ),
                          );
                        }

                        return const CircleAvatar(
                          radius: 50,
                          child: Icon(Icons.person, size: 50),
                        );
                      },
                    ),
                    const SizedBox(height: 16),
                    Text(
                      patient.fullName,
                      style: Theme.of(context).textTheme.headlineSmall,
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '${patient.age} лет',
                      style: Theme.of(context).textTheme.bodyLarge,
                    ),
                    const SizedBox(height: 24),
                    Consumer<AppointmentProvider>(
                      builder: (context, appointmentProvider, child) {
                        final upcomingAppointments =
                            appointmentProvider.upcomingAppointments;
                        if (upcomingAppointments.isEmpty) {
                          return Card(
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: Column(
                                children: [
                                  const Text('Нет предстоящих записей'),
                                  const SizedBox(height: 8),
                                  ElevatedButton(
                                    onPressed: () {
                                      widget.onNavigateToTab?.call(0);
                                    },
                                    child: const Text('Записаться на прием'),
                                  ),
                                ],
                              ),
                            ),
                          );
                        }

                        final nextAppointment = upcomingAppointments.first;
                        return Card(
                          child: Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Предстоящий прием',
                                  style: Theme.of(context).textTheme.titleMedium,
                                ),
                                const SizedBox(height: 12),
                                Text(
                                  nextAppointment.doctorName,
                                  style: const TextStyle(fontWeight: FontWeight.bold),
                                ),
                                if (nextAppointment.doctorSpecialization != null) ...[
                                  const SizedBox(height: 4),
                                  Text(nextAppointment.doctorSpecialization!),
                                ],
                                if (nextAppointment.doctorOffice != null) ...[
                                  const SizedBox(height: 4),
                                  Text('Кабинет: ${nextAppointment.doctorOffice}'),
                                ],
                                const SizedBox(height: 4),
                                Text(
                                  '${DateFormatter.formatDate(nextAppointment.slotDate)} в ${DateFormatter.formatTime(nextAppointment.startTime)}',
                                ),
                                const SizedBox(height: 12),
                                ElevatedButton(
                                  onPressed: () async {
                                    final confirmed = await showDialog<bool>(
                                      context: context,
                                      builder: (context) => AlertDialog(
                                        title: const Text('Отмена записи'),
                                        content: const Text('Вы уверены, что хотите отменить запись?'),
                                        actions: [
                                          TextButton(
                                            onPressed: () => Navigator.pop(context, false),
                                            child: const Text('Нет'),
                                          ),
                                          ElevatedButton(
                                            onPressed: () => Navigator.pop(context, true),
                                            style: ElevatedButton.styleFrom(
                                              backgroundColor: Colors.red,
                                              foregroundColor: Colors.white,
                                            ),
                                            child: const Text('Отменить запись'),
                                          ),
                                        ],
                                      ),
                                    );

                                    if (confirmed == true && mounted) {
                                      final provider = context.read<AppointmentProvider>();
                                      final success = await provider.cancelAppointment(
                                        slotId: nextAppointment.slotId,
                                        reason: 'Отменено пациентом',
                                      );
                                      if (mounted) {
                                        if (success) {
                                          ScaffoldMessenger.of(context).showSnackBar(
                                            const SnackBar(content: Text('Запись отменена')),
                                          );
                                        } else {
                                          ScaffoldMessenger.of(context).showSnackBar(
                                            SnackBar(content: Text('Ошибка: ${provider.error}')),
                                          );
                                        }
                                      }
                                    }
                                  },
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.red,
                                    foregroundColor: Colors.white,
                                    minimumSize: const Size(double.infinity, 40),
                                  ),
                                  child: const Text('Отменить запись'),
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                    const SizedBox(height: 16),
                    Consumer<FavoriteProvider>(
                      builder: (context, favoriteProvider, child) {
                        return Card(
                          child: ListTile(
                            leading: const Icon(Icons.favorite),
                            title: const Text('Избранные врачи'),
                            subtitle: Text(
                              '${favoriteProvider.favorites.length} врачей',
                            ),
                            trailing: const Icon(Icons.chevron_right),
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (_) => const FavoritesScreen(),
                                ),
                              );
                            },
                          ),
                        );
                      },
                    ),
                    const SizedBox(height: 16),
                    Consumer<NotificationProvider>(
                      builder: (context, notificationProvider, child) {
                        final recentNotifications = notificationProvider
                            .notifications
                            .take(3)
                            .toList();

                        if (recentNotifications.isEmpty) {
                          return const SizedBox.shrink();
                        }

                        return Card(
                          child: Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Row(
                                      children: [
                                        const Icon(Icons.notifications),
                                        const SizedBox(width: 8),
                                        Text(
                                          'Последние уведомления',
                                          style: Theme.of(context).textTheme.titleMedium,
                                        ),
                                      ],
                                    ),
                                    TextButton(
                                      onPressed: () {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (_) => const AllNotificationsScreen(),
                                          ),
                                        );
                                      },
                                      child: const Text('Все'),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 12),
                                ...recentNotifications.map((notification) {
                                  return InkWell(
                                    onTap: () async {
                                      print('Notification tapped: type=${notification.type}, link=${notification.link}');

                                      if (notification.type == 'appointment_completed' &&
                                          notification.link != null) {
                                        final appointmentId = int.tryParse(notification.link!);
                                        print('Parsed appointmentId: $appointmentId');

                                        if (appointmentId != null) {
                                          final appointmentProvider = context.read<AppointmentProvider>();

                                          // Ensure appointments are loaded
                                          if (appointmentProvider.myAppointments.isEmpty) {
                                            await appointmentProvider.loadMyAppointments();
                                          }

                                          // Try to find appointment in existing list
                                          final appointment = appointmentProvider.myAppointments
                                              .where((a) => a.idAppointment == appointmentId)
                                              .firstOrNull;

                                          print('Found appointment: ${appointment?.doctorName}');

                                          if (appointment != null && mounted) {
                                            Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                builder: (_) => CreateReviewScreen(
                                                  appointmentId: appointmentId,
                                                  doctorName: appointment.doctorName,
                                                  appointmentDate: appointment.slotDate,
                                                ),
                                              ),
                                            );
                                          } else if (mounted) {
                                            ScaffoldMessenger.of(context).showSnackBar(
                                              const SnackBar(content: Text('Прием не найден')),
                                            );
                                          }
                                        }
                                      }
                                    },
                                    child: Padding(
                                      padding: const EdgeInsets.only(bottom: 12.0),
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            notification.message,
                                            style: const TextStyle(
                                              fontSize: 14,
                                            ),
                                          ),
                                          const SizedBox(height: 4),
                                          Text(
                                            DateFormatter.formatDateTime(
                                                notification.createdAt),
                                            style: Theme.of(context)
                                                .textTheme
                                                .bodySmall
                                                ?.copyWith(
                                                  color: Colors.grey,
                                                  fontSize: 12,
                                                ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  );
                                }),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  ],
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: _logout,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: const Text('Выйти'),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
