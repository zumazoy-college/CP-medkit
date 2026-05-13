import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/appointment_provider.dart';
import '../../providers/review_provider.dart';
import '../../../core/utils/date_formatter.dart';
import '../review/create_review_screen.dart';
import '../review/edit_review_screen.dart';
import '../medical_card/appointment_detail_screen.dart';

class MyAppointmentsScreen extends StatefulWidget {
  const MyAppointmentsScreen({super.key});

  @override
  State<MyAppointmentsScreen> createState() => _MyAppointmentsScreenState();
}

class _MyAppointmentsScreenState extends State<MyAppointmentsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppointmentProvider>().loadMyAppointments();
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _cancelAppointment(int slotId) async {
    final reasonController = TextEditingController();
    String? selectedReason;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Отмена записи'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Укажите причину отмены (необязательно)'),
                const SizedBox(height: 16),
                const Text(
                  'Выберите причину:',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                RadioListTile<String>(
                  title: const Text('Передумал'),
                  value: 'Передумал',
                  groupValue: selectedReason,
                  onChanged: (value) {
                    setState(() {
                      selectedReason = value;
                      reasonController.clear();
                    });
                  },
                  contentPadding: EdgeInsets.zero,
                ),
                RadioListTile<String>(
                  title: const Text('Нашел другое время'),
                  value: 'Нашел другое время',
                  groupValue: selectedReason,
                  onChanged: (value) {
                    setState(() {
                      selectedReason = value;
                      reasonController.clear();
                    });
                  },
                  contentPadding: EdgeInsets.zero,
                ),
                RadioListTile<String>(
                  title: const Text('Заболел'),
                  value: 'Заболел',
                  groupValue: selectedReason,
                  onChanged: (value) {
                    setState(() {
                      selectedReason = value;
                      reasonController.clear();
                    });
                  },
                  contentPadding: EdgeInsets.zero,
                ),
                RadioListTile<String>(
                  title: const Text('Другая причина'),
                  value: 'custom',
                  groupValue: selectedReason,
                  onChanged: (value) {
                    setState(() {
                      selectedReason = value;
                    });
                  },
                  contentPadding: EdgeInsets.zero,
                ),
                if (selectedReason == 'custom') ...[
                  const SizedBox(height: 8),
                  TextField(
                    controller: reasonController,
                    decoration: const InputDecoration(
                      hintText: 'Укажите причину',
                      border: OutlineInputBorder(),
                    ),
                    maxLines: 3,
                  ),
                ],
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Отмена'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Подтвердить'),
            ),
          ],
        ),
      ),
    );

    if (confirmed == true && mounted) {
      String reason = 'Не указана';
      if (selectedReason != null && selectedReason != 'custom') {
        reason = selectedReason!;
      } else if (selectedReason == 'custom' && reasonController.text.isNotEmpty) {
        reason = reasonController.text;
      }

      final success = await context.read<AppointmentProvider>().cancelAppointment(
            slotId: slotId,
            reason: reason,
          );

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Запись успешно отменена')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Мои записи'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Предстоящие'),
            Tab(text: 'Прошедшие'),
          ],
        ),
      ),
      body: Consumer<AppointmentProvider>(
        builder: (context, appointmentProvider, child) {
          if (appointmentProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (appointmentProvider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(appointmentProvider.error!),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () =>
                        appointmentProvider.loadMyAppointments(),
                    child: const Text('Повторить'),
                  ),
                ],
              ),
            );
          }

          return TabBarView(
            controller: _tabController,
            children: [
              _buildAppointmentsList(
                appointmentProvider.upcomingAppointments,
                isUpcoming: true,
              ),
              _buildAppointmentsList(
                appointmentProvider.pastAppointments,
                isUpcoming: false,
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildAppointmentsList(List appointments, {required bool isUpcoming}) {
    if (appointments.isEmpty) {
      return Center(
        child: Text(
          isUpcoming ? 'Нет предстоящих записей' : 'Нет прошедших записей',
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () =>
          context.read<AppointmentProvider>().loadMyAppointments(),
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: appointments.length,
        itemBuilder: (context, index) {
          final appointment = appointments[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 12),
            child: InkWell(
              onTap: !isUpcoming && appointment.status == 'completed'
                  ? () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => AppointmentDetailScreen(
                            appointment: appointment,
                          ),
                        ),
                      );
                    }
                  : null,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                appointment.doctorName,
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 4),
                              if (appointment.doctorSpecialization != null) ...[
                                Text(
                                  appointment.doctorSpecialization!,
                                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                    color: Colors.grey[600],
                                  ),
                                ),
                                const SizedBox(height: 4),
                              ],
                              if (appointment.doctorOffice != null) ...[
                                Text(
                                  'Кабинет: ${appointment.doctorOffice}',
                                  style: Theme.of(context).textTheme.bodySmall,
                                ),
                                const SizedBox(height: 4),
                              ],
                              Text(
                                '${DateFormatter.formatDate(appointment.slotDate)} в ${DateFormatter.formatTime(appointment.startTime)}',
                                style: Theme.of(context).textTheme.bodyMedium,
                              ),
                              if (!isUpcoming && appointment.primaryDiagnosisName != null) ...[
                                const SizedBox(height: 4),
                                Text(
                                  'Диагноз: ${appointment.primaryDiagnosisName}',
                                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                    fontStyle: FontStyle.italic,
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                        if (appointment.status == 'cancelled')
                          Chip(
                            label: const Text('Отменено'),
                            backgroundColor: Colors.red.shade100,
                          ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    if (!isUpcoming && appointment.status == 'completed' && appointment.hasReview != true)
                      ElevatedButton(
                        onPressed: () async {
                          final result = await Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => CreateReviewScreen(
                                appointmentId: appointment.idAppointment,
                                doctorName: appointment.doctorName,
                                appointmentDate: appointment.slotDate,
                              ),
                            ),
                          );
                          if (result == true) {
                            if (!mounted) return;
                            context.read<AppointmentProvider>().loadMyAppointments();
                          }
                        },
                        child: const Text('Оставить отзыв'),
                      ),
                    if (!isUpcoming && appointment.status == 'completed' && appointment.hasReview == true && appointment.reviewRating != null)
                      Row(
                        children: [
                          Expanded(
                            child: Row(
                              children: List.generate(5, (index) {
                                return Icon(
                                  index < appointment.reviewRating!
                                      ? Icons.star
                                      : Icons.star_border,
                                  color: Colors.amber,
                                  size: 20,
                                );
                              }),
                            ),
                          ),
                          if (appointment.canEditReview == true)
                            IconButton(
                              icon: const Icon(Icons.edit, size: 20),
                              tooltip: 'Редактировать отзыв',
                              onPressed: () async {
                                final result = await Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (_) => EditReviewScreen(
                                      reviewId: appointment.reviewId!,
                                      doctorName: appointment.doctorName,
                                      appointmentDate: appointment.slotDate,
                                      initialRating: appointment.reviewRating!,
                                      initialComment: appointment.reviewComment,
                                    ),
                                  ),
                                );
                                if (result == true) {
                                  if (!mounted) return;
                                  context.read<AppointmentProvider>().loadMyAppointments();
                                }
                              },
                            ),
                          if (appointment.canDeleteReview == true)
                            IconButton(
                              icon: const Icon(Icons.delete, size: 20),
                              tooltip: 'Удалить отзыв',
                              color: Colors.red,
                              onPressed: () async {
                                final confirmed = await showDialog<bool>(
                                  context: context,
                                  builder: (context) => AlertDialog(
                                    title: const Text('Удалить отзыв'),
                                    content: const Text('Вы уверены, что хотите удалить свой отзыв?'),
                                    actions: [
                                      TextButton(
                                        onPressed: () => Navigator.pop(context, false),
                                        child: const Text('Отмена'),
                                      ),
                                      ElevatedButton(
                                        onPressed: () => Navigator.pop(context, true),
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: Colors.red,
                                          foregroundColor: Colors.white,
                                        ),
                                        child: const Text('Удалить'),
                                      ),
                                    ],
                                  ),
                                );

                                if (confirmed == true) {
                                  if (!mounted) return;
                                  final success = await context.read<ReviewProvider>().deleteReview(appointment.reviewId!);
                                  if (success) {
                                    if (!mounted) return;
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      const SnackBar(content: Text('Отзыв успешно удален')),
                                    );
                                    await context.read<AppointmentProvider>().loadMyAppointments();
                                  }
                                }
                              },
                            ),
                        ],
                      ),
                    if (isUpcoming && appointment.status != 'cancelled')
                      ElevatedButton(
                        onPressed: () => _cancelAppointment(appointment.slotId),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.red,
                          foregroundColor: Colors.white,
                        ),
                        child: const Text('Отменить запись'),
                      ),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
