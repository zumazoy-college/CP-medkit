import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import '../../providers/appointment_provider.dart';
import '../../providers/medical_record_provider.dart';
import '../../../core/utils/date_formatter.dart';
import 'appointment_detail_screen.dart';

enum MedicalCardFilter { all, diagnoses, prescriptions, analyses, files }

class MedicalCardScreen extends StatefulWidget {
  const MedicalCardScreen({super.key});

  @override
  State<MedicalCardScreen> createState() => _MedicalCardScreenState();
}

class _MedicalCardScreenState extends State<MedicalCardScreen> {
  MedicalCardFilter _currentFilter = MedicalCardFilter.all;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppointmentProvider>().loadMyAppointments();
      context.read<MedicalRecordProvider>().loadAllPrescriptions();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Медицинская карта'),
      ),
      body: Column(
        children: [
          _buildFilterChips(),
          Expanded(
            child: _buildContent(),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChips() {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          _buildFilterChip('Все записи', MedicalCardFilter.all),
          const SizedBox(width: 8),
          _buildFilterChip('Диагнозы', MedicalCardFilter.diagnoses),
          const SizedBox(width: 8),
          _buildFilterChip('Назначения', MedicalCardFilter.prescriptions),
          const SizedBox(width: 8),
          _buildFilterChip('Анализы', MedicalCardFilter.analyses),
          const SizedBox(width: 8),
          _buildFilterChip('Файлы', MedicalCardFilter.files),
        ],
      ),
    );
  }

  Widget _buildFilterChip(String label, MedicalCardFilter filter) {
    final isSelected = _currentFilter == filter;
    return FilterChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (selected) {
        setState(() {
          _currentFilter = filter;
        });
      },
    );
  }

  Widget _buildContent() {
    if (_currentFilter == MedicalCardFilter.all) {
      return _buildAllRecords();
    } else if (_currentFilter == MedicalCardFilter.diagnoses) {
      return _buildDiagnoses();
    } else if (_currentFilter == MedicalCardFilter.prescriptions) {
      return _buildPrescriptions();
    } else if (_currentFilter == MedicalCardFilter.analyses) {
      return _buildAnalyses();
    } else if (_currentFilter == MedicalCardFilter.files) {
      return _buildFiles();
    } else {
      return _buildAllRecords();
    }
  }

  Widget _buildAllRecords() {
    return Consumer<AppointmentProvider>(
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

        final completedAppointments = appointmentProvider.myAppointments
            .where((a) => a.status == 'completed')
            .toList()
          ..sort((a, b) => b.slotDate.compareTo(a.slotDate));

        if (completedAppointments.isEmpty) {
          return const Center(
            child: Text('Нет записей в медицинской карте'),
          );
        }

        return RefreshIndicator(
          onRefresh: () => appointmentProvider.loadMyAppointments(),
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: completedAppointments.length,
            itemBuilder: (context, index) {
              final appointment = completedAppointments[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: InkWell(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => AppointmentDetailScreen(
                          appointment: appointment,
                        ),
                      ),
                    );
                  },
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Icon(
                              Icons.calendar_today,
                              size: 16,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              DateFormatter.formatDate(appointment.slotDate),
                              style: Theme.of(context).textTheme.titleSmall,
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Icon(
                              Icons.access_time,
                              size: 16,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              '${DateFormatter.formatTime(appointment.startTime)} - ${DateFormatter.formatTime(appointment.endTime)}',
                              style: Theme.of(context).textTheme.bodySmall,
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(
                          appointment.doctorName,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        if (appointment.doctorSpecialization != null) ...[
                          const SizedBox(height: 4),
                          Text(
                            appointment.doctorSpecialization!,
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: Colors.grey[600],
                            ),
                          ),
                        ],
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            const Icon(Icons.chevron_right, size: 16),
                            const SizedBox(width: 4),
                            Text(
                              'Подробнее',
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.primary,
                              ),
                            ),
                          ],
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
    );
  }

  Widget _buildDiagnoses() {
    return Consumer<MedicalRecordProvider>(
      builder: (context, medicalRecordProvider, child) {
        if (medicalRecordProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (medicalRecordProvider.diagnoses.isEmpty) {
          return const Center(
            child: Text('Нет диагнозов'),
          );
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: medicalRecordProvider.diagnoses.length,
          itemBuilder: (context, index) {
            final diagnosis = medicalRecordProvider.diagnoses[index];
            return Card(
              margin: const EdgeInsets.only(bottom: 12),
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
                    const SizedBox(height: 8),
                    Text(
                      'Дата: ${DateFormatter.formatDate(diagnosis.appointmentDate)}',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildFiles() {
    return Consumer<MedicalRecordProvider>(
      builder: (context, medicalRecordProvider, child) {
        if (medicalRecordProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (medicalRecordProvider.files.isEmpty) {
          return const Center(
            child: Text('Нет прикрепленных файлов'),
          );
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: medicalRecordProvider.files.length,
          itemBuilder: (context, index) {
            final file = medicalRecordProvider.files[index];
            return FutureBuilder<bool>(
              future: _checkFileExists(file.fileName),
              builder: (context, snapshot) {
                final fileExists = snapshot.data ?? false;
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
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
          },
        );
      },
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

        // Update UI to show file is now downloaded
        setState(() {});

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

  Widget _buildPrescriptions() {
    return Consumer<MedicalRecordProvider>(
      builder: (context, medicalRecordProvider, child) {
        if (medicalRecordProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        final hasMedications = medicalRecordProvider.medications.isNotEmpty;
        final hasProcedures = medicalRecordProvider.procedures.isNotEmpty;

        if (!hasMedications && !hasProcedures) {
          return const Center(
            child: Text('Нет назначений'),
          );
        }

        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            if (hasMedications) ...[
              Text(
                'Лекарства',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              ...medicalRecordProvider.medications.map((medication) {
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          medication.medicationName,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 8),
                        if (medication.dosage != null)
                          Text('Дозировка: ${medication.dosage}'),
                        if (medication.frequency != null)
                          Text('Частота: ${medication.frequency}'),
                        if (medication.duration != null)
                          Text('Длительность: ${medication.duration}'),
                        if (medication.instructions != null) ...[
                          const SizedBox(height: 8),
                          Text(
                            medication.instructions!,
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                        const SizedBox(height: 8),
                        Text(
                          'Назначено: ${DateFormatter.formatDate(medication.createdAt)}',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                );
              }),
              if (hasProcedures) const SizedBox(height: 24),
            ],
            if (hasProcedures) ...[
              Text(
                'Процедуры',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              ...medicalRecordProvider.procedures.map((procedure) {
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          procedure.procedureName,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 8),
                        if (procedure.instructions != null) ...[
                          const SizedBox(height: 8),
                          Text(
                            procedure.instructions!,
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                        const SizedBox(height: 8),
                        Text(
                          'Назначено: ${DateFormatter.formatDate(procedure.createdAt)}',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                );
              }),
            ],
          ],
        );
      },
    );
  }

  Widget _buildMedications() {
    return Consumer<MedicalRecordProvider>(
      builder: (context, medicalRecordProvider, child) {
        if (medicalRecordProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (medicalRecordProvider.medications.isEmpty) {
          return const Center(
            child: Text('Нет назначенных лекарств'),
          );
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: medicalRecordProvider.medications.length,
          itemBuilder: (context, index) {
            final medication = medicalRecordProvider.medications[index];
            return Card(
              margin: const EdgeInsets.only(bottom: 12),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            medication.medicationName,
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ),
                        _buildStatusChip(medication.status),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (medication.dosage != null)
                      Text('Дозировка: ${medication.dosage}'),
                    if (medication.frequency != null)
                      Text('Частота: ${medication.frequency}'),
                    if (medication.duration != null)
                      Text('Длительность: ${medication.duration}'),
                    if (medication.instructions != null) ...[
                      const SizedBox(height: 8),
                      Text(
                        medication.instructions!,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                    const SizedBox(height: 8),
                    Text(
                      'Назначено: ${DateFormatter.formatDate(medication.createdAt)}',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildProcedures() {
    return Consumer<MedicalRecordProvider>(
      builder: (context, medicalRecordProvider, child) {
        if (medicalRecordProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (medicalRecordProvider.procedures.isEmpty) {
          return const Center(
            child: Text('Нет назначенных процедур'),
          );
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: medicalRecordProvider.procedures.length,
          itemBuilder: (context, index) {
            final procedure = medicalRecordProvider.procedures[index];
            return Card(
              margin: const EdgeInsets.only(bottom: 12),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            procedure.procedureName,
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ),
                        _buildStatusChip(procedure.status),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (procedure.instructions != null) ...[
                      const SizedBox(height: 8),
                      Text(
                        procedure.instructions!,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                    const SizedBox(height: 8),
                    Text(
                      'Назначено: ${DateFormatter.formatDate(procedure.createdAt)}',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildAnalyses() {
    return Consumer<MedicalRecordProvider>(
      builder: (context, medicalRecordProvider, child) {
        if (medicalRecordProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (medicalRecordProvider.analyses.isEmpty) {
          return const Center(
            child: Text('Нет назначенных анализов'),
          );
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: medicalRecordProvider.analyses.length,
          itemBuilder: (context, index) {
            final analysis = medicalRecordProvider.analyses[index];
            return Card(
              margin: const EdgeInsets.only(bottom: 12),
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
                      const SizedBox(height: 8),
                      Text(
                        analysis.instructions!,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                    const SizedBox(height: 8),
                    Text(
                      'Назначено: ${DateFormatter.formatDate(analysis.createdAt)}',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildStatusChip(String status) {
    Color color;
    String label;

    switch (status.toLowerCase()) {
      case 'active':
        color = Colors.green;
        label = 'Активно';
        break;
      case 'completed':
        color = Colors.blue;
        label = 'Выполнено';
        break;
      case 'cancelled':
        color = Colors.red;
        label = 'Отменено';
        break;
      default:
        color = Colors.grey;
        label = status;
    }

    return Chip(
      label: Text(
        label,
        style: const TextStyle(fontSize: 12),
      ),
      backgroundColor: color.withValues(alpha: 0.2),
      labelStyle: TextStyle(color: color),
      padding: EdgeInsets.zero,
      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
    );
  }
}
