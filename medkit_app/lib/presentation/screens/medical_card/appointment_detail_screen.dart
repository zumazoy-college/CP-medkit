import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import '../../providers/appointment_provider.dart';
import '../../providers/review_provider.dart';
import '../../providers/medical_record_provider.dart';
import '../../../data/models/appointment_model.dart';
import '../../../core/utils/date_formatter.dart';
import '../review/create_review_screen.dart';
import '../review/edit_review_screen.dart';

class AppointmentDetailScreen extends StatefulWidget {
  final AppointmentModel appointment;

  const AppointmentDetailScreen({
    super.key,
    required this.appointment,
  });

  @override
  State<AppointmentDetailScreen> createState() =>
      _AppointmentDetailScreenState();
}

class _AppointmentDetailScreenState extends State<AppointmentDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context
          .read<AppointmentProvider>()
          .loadAppointmentDetails(widget.appointment.idAppointment);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Детали приема'),
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
                    onPressed: () => appointmentProvider.loadAppointmentDetails(
                        widget.appointment.idAppointment),
                    child: const Text('Повторить'),
                  ),
                ],
              ),
            );
          }

          final detailedAppointment = appointmentProvider.detailedAppointment;
          if (detailedAppointment == null) {
            return const Center(child: CircularProgressIndicator());
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Информация о приеме',
                          style: Theme.of(context).textTheme.titleLarge,
                        ),
                        const SizedBox(height: 16),
                        _buildInfoRow(
                          Icons.calendar_today,
                          'Дата',
                          DateFormatter.formatDate(detailedAppointment.slotDate),
                        ),
                        const SizedBox(height: 8),
                        _buildInfoRow(
                          Icons.access_time,
                          'Время',
                          '${DateFormatter.formatTime(detailedAppointment.startTime)} - ${DateFormatter.formatTime(detailedAppointment.endTime)}',
                        ),
                        const SizedBox(height: 8),
                        _buildInfoRow(
                          Icons.person,
                          'Врач',
                          detailedAppointment.doctor.fullName,
                        ),
                        const SizedBox(height: 8),
                        _buildInfoRow(
                          Icons.medical_services,
                          'Специализация',
                          detailedAppointment.doctor.specialization,
                        ),
                      ],
                    ),
                  ),
                ),
                if (detailedAppointment.complaints != null) ...[
                  const SizedBox(height: 16),
                  Text(
                    'Жалобы',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(detailedAppointment.complaints!),
                    ),
                  ),
                ],
                if (detailedAppointment.anamnesis != null) ...[
                  const SizedBox(height: 16),
                  Text(
                    'Анамнез',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(detailedAppointment.anamnesis!),
                    ),
                  ),
                ],
                if (detailedAppointment.objectiveData != null) ...[
                  const SizedBox(height: 16),
                  Text(
                    'Объективные данные',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(detailedAppointment.objectiveData!),
                    ),
                  ),
                ],
                const SizedBox(height: 16),
                Text(
                  'Диагнозы',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 8),
                _buildDiagnoses(detailedAppointment.diagnoses, context),
                const SizedBox(height: 16),
                Text(
                  'Назначения',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 8),
                _buildPrescriptions(
                  detailedAppointment.medications,
                  detailedAppointment.procedures,
                  detailedAppointment.analyses,
                  context,
                ),
                if (detailedAppointment.recommendations != null) ...[
                  const SizedBox(height: 16),
                  Text(
                    'Рекомендации',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(detailedAppointment.recommendations!),
                    ),
                  ),
                ],
                if (detailedAppointment.files.isNotEmpty) ...[
                  const SizedBox(height: 16),
                  Text(
                    'Прикрепленные файлы',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  _buildFiles(detailedAppointment.files, context),
                ],
                const SizedBox(height: 16),
                _buildReviewSection(context),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildDiagnoses(List diagnoses, BuildContext context) {
    if (diagnoses.isEmpty) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Text('Диагнозы не указаны'),
        ),
      );
    }

    return Column(
      children: diagnoses.map((diagnosis) {
        return Card(
          margin: const EdgeInsets.only(bottom: 8),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 4,
                      ),
                      decoration: BoxDecoration(
                        color: Theme.of(context).colorScheme.primaryContainer,
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        diagnosis.icdCode,
                        style: TextStyle(
                          color: Theme.of(context).colorScheme.onPrimaryContainer,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        diagnosis.diagnosisName,
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                    ),
                    if (diagnosis.isPrimary == true)
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: Colors.green.withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text(
                          'Основной',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.green,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                  ],
                ),
                if (diagnosis.notes != null) ...[
                  const SizedBox(height: 8),
                  Text(diagnosis.notes!),
                ],
              ],
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildPrescriptions(
    List medications,
    List procedures,
    List analyses,
    BuildContext context,
  ) {
    if (medications.isEmpty && procedures.isEmpty && analyses.isEmpty) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Text('Назначения отсутствуют'),
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (medications.isNotEmpty) ...[
          Text(
            'Лекарства',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          ...medications.map((medication) {
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      medication.medicationName,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 4),
                    if (medication.dosage != null)
                      Text('Дозировка: ${medication.dosage}'),
                    if (medication.frequency != null)
                      Text('Частота: ${medication.frequency}'),
                    if (medication.duration != null)
                      Text('Длительность: ${medication.duration}'),
                    if (medication.instructions != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        medication.instructions!,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ],
                ),
              ),
            );
          }),
          const SizedBox(height: 16),
        ],
        if (procedures.isNotEmpty) ...[
          Text(
            'Процедуры',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          ...procedures.map((procedure) {
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      procedure.procedureName,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    if (procedure.instructions != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        procedure.instructions!,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ],
                ),
              ),
            );
          }),
          const SizedBox(height: 16),
        ],
        if (analyses.isNotEmpty) ...[
          Text(
            'Анализы',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          ...analyses.map((analysis) {
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      analysis.analysisName,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    if (analysis.instructions != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        analysis.instructions!,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ],
                ),
              ),
            );
          }),
        ],
      ],
    );
  }

  Widget _buildFiles(List files, BuildContext context) {
    return Column(
      children: files.map((file) {
        return FutureBuilder<bool>(
          future: _checkFileExists(file.fileName),
          builder: (context, snapshot) {
            final fileExists = snapshot.data ?? false;
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              child: ListTile(
                leading: Icon(
                  _getFileIcon(file.fileName),
                  size: 32,
                  color: Theme.of(context).colorScheme.primary,
                ),
                title: Text(file.fileName),
                subtitle: Text(
                  'Загружено: ${DateFormatter.formatDate(file.uploadedAt)}${fileExists ? ' • Скачан' : ''}',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
                trailing: IconButton(
                  icon: Icon(fileExists ? Icons.open_in_new : Icons.download),
                  tooltip: fileExists ? 'Открыть файл' : 'Скачать файл',
                  onPressed: () async {
                    await _handleFileAction(context, file, fileExists);
                  },
                ),
              ),
            );
          },
        );
      }).toList(),
    );
  }

  Future<bool> _checkFileExists(String fileName) async {
    try {
      // Try to get Downloads directory, fallback to app directory
      Directory? directory;
      if (Platform.isAndroid) {
        directory = Directory('/storage/emulated/0/Download');
        if (!await directory.exists()) {
          directory = await getExternalStorageDirectory();
        }
      } else {
        directory = await getApplicationDocumentsDirectory();
      }

      final filePath = '${directory!.path}/$fileName';
      return await File(filePath).exists();
    } catch (e) {
      return false;
    }
  }

  Future<void> _handleFileAction(BuildContext context, dynamic file, bool fileExists) async {
    try {
      // Try to get Downloads directory, fallback to app directory
      Directory? directory;
      if (Platform.isAndroid) {
        directory = Directory('/storage/emulated/0/Download');
        if (!await directory.exists()) {
          directory = await getExternalStorageDirectory();
        }
      } else {
        directory = await getApplicationDocumentsDirectory();
      }

      final filePath = '${directory!.path}/${file.fileName}';

      if (fileExists) {
        // File already downloaded, show info
        if (!context.mounted) return;
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Файл сохранен'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Файл: ${file.fileName}'),
                const SizedBox(height: 8),
                const Text('Путь:', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                SelectableText(
                  filePath,
                  style: const TextStyle(fontSize: 12),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Откройте файл через файловый менеджер или приложение для просмотра.',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('OK'),
              ),
            ],
          ),
        );
      } else {
        // Download and save file
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Загрузка файла...')),
        );

        if (!context.mounted) return;
        final bytes = await context.read<MedicalRecordProvider>().downloadFile(file.id);

        // Save file
        final savedFile = File(filePath);
        await savedFile.writeAsBytes(bytes);

        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Файл ${file.fileName} сохранен')),
        );

        // Show success dialog with path
        if (!context.mounted) return;
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Файл загружен'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(Icons.check_circle, color: Colors.green, size: 48),
                const SizedBox(height: 16),
                Text('Файл: ${file.fileName}'),
                const SizedBox(height: 8),
                const Text('Путь:', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                SelectableText(
                  filePath,
                  style: const TextStyle(fontSize: 12),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Откройте файл через файловый менеджер или приложение для просмотра.',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('OK'),
              ),
            ],
          ),
        );
      }
    } catch (e) {
      if (!context.mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Ошибка: $e')),
      );
    }
  }

  IconData _getFileIcon(String fileName) {
    final extension = fileName.split('.').last.toLowerCase();
    switch (extension) {
      case 'pdf':
        return Icons.picture_as_pdf;
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif':
        return Icons.image;
      case 'doc':
      case 'docx':
        return Icons.description;
      default:
        return Icons.insert_drive_file;
    }
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 20),
        const SizedBox(width: 8),
        Text(
          '$label: ',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        Expanded(child: Text(value)),
      ],
    );
  }

  Widget _buildReviewSection(BuildContext context) {
    final appointment = widget.appointment;

    if (appointment.hasReview == true && appointment.reviewRating != null) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Ваш отзыв',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 12),
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
                          size: 24,
                        );
                      }),
                    ),
                  ),
                  if (appointment.canEditReview == true)
                    IconButton(
                      icon: const Icon(Icons.edit),
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
                          await context.read<AppointmentProvider>().loadMyAppointments();
                          if (!mounted) return;
                          await context.read<AppointmentProvider>().loadAppointmentDetails(appointment.idAppointment);
                        }
                      },
                    ),
                  if (appointment.canDeleteReview == true)
                    IconButton(
                      icon: const Icon(Icons.delete),
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
                            if (!mounted) return;
                            await context.read<AppointmentProvider>().loadAppointmentDetails(appointment.idAppointment);
                          }
                        }
                      },
                    ),
                ],
              ),
              if (appointment.reviewComment != null && appointment.reviewComment!.isNotEmpty) ...[
                const SizedBox(height: 8),
                Text(
                  appointment.reviewComment!,
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ],
          ),
        ),
      );
    } else if (appointment.status == 'completed' && appointment.hasReview != true) {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton(
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
              await context.read<AppointmentProvider>().loadMyAppointments();
              if (!mounted) return;
              await context.read<AppointmentProvider>().loadAppointmentDetails(appointment.idAppointment);
            }
          },
          child: const Text('Оставить отзыв'),
        ),
      );
    }

    return const SizedBox.shrink();
  }
}
