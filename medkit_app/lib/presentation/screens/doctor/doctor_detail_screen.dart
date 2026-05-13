import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import '../../providers/doctor_provider.dart';
import '../../providers/review_provider.dart';
import '../../providers/favorite_provider.dart';
import '../../providers/patient_provider.dart';
import '../../providers/appointment_provider.dart';
import '../../../core/utils/date_formatter.dart';
import '../../../core/constants/api_constants.dart';

class DoctorDetailScreen extends StatefulWidget {
  final int doctorId;

  const DoctorDetailScreen({super.key, required this.doctorId});

  @override
  State<DoctorDetailScreen> createState() => _DoctorDetailScreenState();
}

class _DoctorDetailScreenState extends State<DoctorDetailScreen>
    with WidgetsBindingObserver {
  DateTime _selectedDate = DateTime.now();
  DateTime? _selectedSlot;

  String? _getAvatarUrl(String? avatarPath) {
    if (avatarPath == null || avatarPath.isEmpty) return null;
    if (avatarPath.startsWith('http')) return avatarPath;

    // Get base URL without /api suffix
    final baseUrl = ApiConstants.baseUrl.replaceAll('/api', '');
    return '$baseUrl$avatarPath';
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DoctorProvider>().loadDoctorById(widget.doctorId);
      context.read<ReviewProvider>().loadDoctorReviews(widget.doctorId);
      context.read<ReviewProvider>().loadDoctorRatingStats(widget.doctorId);
      _loadSlots();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      // Reload slots when app returns to foreground
      _loadSlots();
    }
  }

  @override
  void didUpdateWidget(DoctorDetailScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.doctorId != widget.doctorId) {
      _loadSlots();
    }
  }

  void _loadSlots() async {
    final appointmentProvider = context.read<AppointmentProvider>();

    await appointmentProvider.loadAvailableSlots(
          doctorId: widget.doctorId,
          date: _selectedDate,
        );

    // Auto-select next available date if no slots available for selected date
    if (!mounted) return;

    if (appointmentProvider.availableSlots.isEmpty) {
      // Try to find next available date within 90 days
      DateTime nextDate = _selectedDate.add(const Duration(days: 1));
      final maxDate = DateTime.now().add(const Duration(days: 90));

      while (nextDate.isBefore(maxDate) || nextDate.isAtSameMomentAs(maxDate)) {
        await appointmentProvider.loadAvailableSlots(
          doctorId: widget.doctorId,
          date: nextDate,
        );

        if (!mounted) return;

        if (appointmentProvider.availableSlots.isNotEmpty) {
          // Found available slots, update selected date
          setState(() {
            _selectedDate = nextDate;
          });
          break;
        }

        nextDate = nextDate.add(const Duration(days: 1));
      }
    }
  }

  Future<void> _selectDate() async {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);

    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate.isBefore(today) ? today : _selectedDate,
      firstDate: today,
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );

    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
        _selectedSlot = null;
      });
      _loadSlots();
    }
  }

  Future<void> _bookAppointment() async {
    if (_selectedSlot == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Выберите время приема')),
      );
      return;
    }

    final doctor = context.read<DoctorProvider>().selectedDoctor;
    if (doctor == null) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Подтверждение записи'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Врач: ${doctor.fullName}'),
            if (doctor.office != null) Text('Кабинет: ${doctor.office}'),
            Text('Дата: ${DateFormatter.formatDate(_selectedDate)}'),
            Text('Время: ${DateFormatter.formatTime(_selectedSlot!)}'),
          ],
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
    );

    if (confirmed == true && mounted) {
      final success = await context.read<AppointmentProvider>().bookAppointment(
            doctorId: widget.doctorId,
            slotDate: _selectedDate,
            startTime: _selectedSlot!,
          );

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Запись успешно создана')),
        );
        setState(() {
          _selectedSlot = null;
        });
        _loadSlots();
        context.read<DoctorProvider>().loadDoctors();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Информация о враче'),
        actions: [
          Consumer<FavoriteProvider>(
            builder: (context, favoriteProvider, child) {
              final isFavorite = favoriteProvider.isFavorite(widget.doctorId);
              return IconButton(
                icon: Icon(
                  isFavorite ? Icons.favorite : Icons.favorite_border,
                  color: isFavorite ? Colors.red : null,
                ),
                onPressed: () async {
                  await favoriteProvider.toggleFavorite(widget.doctorId);
                },
              );
            },
          ),
        ],
      ),
      body: Consumer<DoctorProvider>(
        builder: (context, doctorProvider, child) {
          if (doctorProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (doctorProvider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(doctorProvider.error!),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () =>
                        doctorProvider.loadDoctorById(widget.doctorId),
                    child: const Text('Повторить'),
                  ),
                ],
              ),
            );
          }

          final doctor = doctorProvider.selectedDoctor;
          if (doctor == null) {
            return const Center(child: Text('Врач не найден'));
          }

          return SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Container(
                  padding: const EdgeInsets.all(24),
                  color: Theme.of(context).colorScheme.primaryContainer,
                  child: Column(
                    children: [
                      CircleAvatar(
                        radius: 50,
                        backgroundImage: _getAvatarUrl(doctor.avatarUrl) != null
                            ? NetworkImage(_getAvatarUrl(doctor.avatarUrl)!)
                            : null,
                        child: _getAvatarUrl(doctor.avatarUrl) == null
                            ? const Icon(Icons.person, size: 50)
                            : null,
                      ),
                      const SizedBox(height: 16),
                      Text(
                        doctor.fullName,
                        style: Theme.of(context).textTheme.headlineSmall,
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 8),
                      Text(
                        doctor.specialization,
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 16),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          RatingBarIndicator(
                            rating: doctor.rating,
                            itemBuilder: (context, _) => const Icon(
                              Icons.star,
                              color: Colors.amber,
                            ),
                            itemCount: 5,
                            itemSize: 24,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            '${doctor.rating.toStringAsFixed(1)} (${doctor.reviewsCount})',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (doctor.workExperience != null) ...[
                        _InfoRow(
                          icon: Icons.work,
                          label: 'Стаж',
                          value: doctor.workExperience!,
                        ),
                        const SizedBox(height: 12),
                      ],
                      if (doctor.office != null) ...[
                        _InfoRow(
                          icon: Icons.room,
                          label: 'Кабинет',
                          value: doctor.office!,
                        ),
                        const SizedBox(height: 12),
                      ],
                      const SizedBox(height: 24),
                      Text(
                        'Запись на прием',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 16),
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: InkWell(
                                      onTap: _selectDate,
                                      child: InputDecorator(
                                        decoration: const InputDecoration(
                                          labelText: 'Дата приема',
                                          prefixIcon: Icon(Icons.calendar_today),
                                        ),
                                        child: Text(
                                          DateFormatter.formatDate(_selectedDate),
                                        ),
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  IconButton(
                                    icon: const Icon(Icons.refresh),
                                    onPressed: _loadSlots,
                                    tooltip: 'Обновить слоты',
                                  ),
                                ],
                              ),
                              const SizedBox(height: 16),
                              Text(
                                'Доступное время',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 12),
                              Consumer<AppointmentProvider>(
                                builder: (context, appointmentProvider, child) {
                                  if (appointmentProvider.isLoading) {
                                    return const Center(
                                      child: Padding(
                                        padding: EdgeInsets.all(16.0),
                                        child: CircularProgressIndicator(),
                                      ),
                                    );
                                  }

                                  final availableSlots = appointmentProvider.availableSlots
                                      .where((slot) => slot.isAvailable)
                                      .toList();

                                  if (availableSlots.isEmpty) {
                                    return const Padding(
                                      padding: EdgeInsets.all(16.0),
                                      child: Text(
                                        'Нет доступных слотов на эту дату',
                                        textAlign: TextAlign.center,
                                      ),
                                    );
                                  }

                                  return Wrap(
                                    spacing: 8,
                                    runSpacing: 8,
                                    children: availableSlots.map((slot) {
                                      final isSelected = _selectedSlot != null &&
                                          DateFormatter.formatTime(_selectedSlot!) ==
                                              DateFormatter.formatTime(slot.startTime);

                                      return InkWell(
                                        onTap: () {
                                          if (isSelected) {
                                            // If already selected, open confirmation dialog
                                            _bookAppointment();
                                          } else {
                                            // Otherwise, just select it
                                            setState(() {
                                              _selectedSlot = slot.startTime;
                                            });
                                          }
                                        },
                                        child: Container(
                                          padding: const EdgeInsets.symmetric(
                                            horizontal: 16,
                                            vertical: 8,
                                          ),
                                          decoration: BoxDecoration(
                                            color: isSelected
                                                ? Theme.of(context).colorScheme.primary
                                                : Theme.of(context).colorScheme.surfaceContainerHighest,
                                            borderRadius: BorderRadius.circular(8),
                                          ),
                                          child: Text(
                                            DateFormatter.formatTime(slot.startTime),
                                            style: TextStyle(
                                              color: isSelected
                                                  ? Theme.of(context).colorScheme.onPrimary
                                                  : null,
                                              fontWeight: isSelected ? FontWeight.bold : null,
                                            ),
                                          ),
                                        ),
                                      );
                                    }).toList(),
                                  );
                                },
                              ),
                              const SizedBox(height: 16),
                              ElevatedButton(
                                onPressed: _bookAppointment,
                                style: ElevatedButton.styleFrom(
                                  minimumSize: const Size(double.infinity, 48),
                                ),
                                child: const Text('Подтвердить запись'),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 24),
                      Text(
                        'Отзывы',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 16),
                      Consumer<ReviewProvider>(
                        builder: (context, reviewProvider, child) {
                          if (reviewProvider.ratingStats != null) {
                            final stats = reviewProvider.ratingStats!;
                            return Card(
                              child: Padding(
                                padding: const EdgeInsets.all(16.0),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      'Распределение оценок',
                                      style: Theme.of(context).textTheme.titleMedium,
                                    ),
                                    const SizedBox(height: 12),
                                    for (int i = 5; i >= 1; i--)
                                      Padding(
                                        padding: const EdgeInsets.only(bottom: 8.0),
                                        child: Row(
                                          children: [
                                            Text('$i'),
                                            const SizedBox(width: 8),
                                            const Icon(Icons.star, size: 16, color: Colors.amber),
                                            const SizedBox(width: 8),
                                            Expanded(
                                              child: LinearProgressIndicator(
                                                value: stats.totalReviews > 0
                                                    ? stats.getCountForRating(i) / stats.totalReviews
                                                    : 0,
                                                backgroundColor: Colors.grey[300],
                                                valueColor: const AlwaysStoppedAnimation<Color>(Colors.amber),
                                              ),
                                            ),
                                            const SizedBox(width: 8),
                                            SizedBox(
                                              width: 50,
                                              child: Text(
                                                '${stats.getPercentageForRating(i).toStringAsFixed(0)}%',
                                                textAlign: TextAlign.right,
                                                style: Theme.of(context).textTheme.bodySmall,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                  ],
                                ),
                              ),
                            );
                          }
                          return const SizedBox.shrink();
                        },
                      ),
                      const SizedBox(height: 16),
                      Consumer<ReviewProvider>(
                        builder: (context, reviewProvider, child) {
                          if (reviewProvider.isLoading) {
                            return const Center(
                              child: CircularProgressIndicator(),
                            );
                          }

                          if (reviewProvider.reviews.isEmpty) {
                            return const Center(
                              child: Padding(
                                padding: EdgeInsets.all(32.0),
                                child: Text('Пока нет отзывов'),
                              ),
                            );
                          }

                          return ListView.builder(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            itemCount: reviewProvider.reviews.length,
                            itemBuilder: (context, index) {
                              final review = reviewProvider.reviews[index];
                              final isMyReview = context
                                      .read<PatientProvider>()
                                      .currentPatient
                                      ?.idPatient ==
                                  review.patientId;
                              return Card(
                                margin: const EdgeInsets.only(bottom: 12),
                                child: Padding(
                                  padding: const EdgeInsets.all(16.0),
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Row(
                                        children: [
                                          Expanded(
                                            child: Text(
                                              review.patientName,
                                              style: Theme.of(context)
                                                  .textTheme
                                                  .titleSmall,
                                            ),
                                          ),
                                          Text(
                                            DateFormatter.formatDate(
                                                review.createdAt),
                                            style: Theme.of(context)
                                                .textTheme
                                                .bodySmall,
                                          ),
                                          if (isMyReview && review.canDelete)
                                            IconButton(
                                              icon: const Icon(Icons.delete,
                                                  color: Colors.red),
                                              onPressed: () async {
                                                final confirmed =
                                                    await showDialog<bool>(
                                                  context: context,
                                                  builder: (context) =>
                                                      AlertDialog(
                                                    title: const Text(
                                                        'Удаление отзыва'),
                                                    content: const Text(
                                                        'Вы уверены, что хотите удалить отзыв?'),
                                                    actions: [
                                                      TextButton(
                                                        onPressed: () =>
                                                            Navigator.pop(
                                                                context, false),
                                                        child: const Text(
                                                            'Отмена'),
                                                      ),
                                                      ElevatedButton(
                                                        onPressed: () =>
                                                            Navigator.pop(
                                                                context, true),
                                                        style: ElevatedButton
                                                            .styleFrom(
                                                          backgroundColor:
                                                              Colors.red,
                                                          foregroundColor:
                                                              Colors.white,
                                                        ),
                                                        child: const Text(
                                                            'Удалить'),
                                                      ),
                                                    ],
                                                  ),
                                                );

                                                if (confirmed == true &&
                                                    context.mounted) {
                                                  final success = await context
                                                      .read<ReviewProvider>()
                                                      .deleteReview(
                                                          review.idReview);
                                                  if (success &&
                                                      context.mounted) {
                                                    ScaffoldMessenger.of(
                                                            context)
                                                        .showSnackBar(
                                                      const SnackBar(
                                                        content: Text(
                                                            'Отзыв удален'),
                                                      ),
                                                    );
                                                  }
                                                }
                                              },
                                            ),
                                        ],
                                      ),
                                      const SizedBox(height: 8),
                                      RatingBarIndicator(
                                        rating: review.rating.toDouble(),
                                        itemBuilder: (context, _) =>
                                            const Icon(
                                          Icons.star,
                                          color: Colors.amber,
                                        ),
                                        itemCount: 5,
                                        itemSize: 16,
                                      ),
                                      if (review.comment != null) ...[
                                        const SizedBox(height: 8),
                                        Text(review.comment!),
                                      ],
                                    ],
                                  ),
                                ),
                              );
                            },
                          );
                        },
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _InfoRow({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 20),
        const SizedBox(width: 8),
        Text(
          '$label: ',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
        ),
        Text(value),
      ],
    );
  }
}
