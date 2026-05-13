import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/appointment_provider.dart';
import '../../../core/utils/date_formatter.dart';

class BookAppointmentScreen extends StatefulWidget {
  final int doctorId;
  final String doctorName;
  final String? doctorOffice;

  const BookAppointmentScreen({
    super.key,
    required this.doctorId,
    required this.doctorName,
    this.doctorOffice,
  });

  @override
  State<BookAppointmentScreen> createState() => _BookAppointmentScreenState();
}

class _BookAppointmentScreenState extends State<BookAppointmentScreen> {
  DateTime _selectedDate = DateTime.now();
  DateTime? _selectedSlot;

  @override
  void initState() {
    super.initState();
    _loadSlots();
  }

  void _loadSlots() {
    context.read<AppointmentProvider>().loadAvailableSlots(
          doctorId: widget.doctorId,
          date: _selectedDate,
        );
  }

  Future<void> _selectDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime.now(),
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

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Подтверждение записи'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Врач: ${widget.doctorName}'),
            if (widget.doctorOffice != null)
              Text('Кабинет: ${widget.doctorOffice}'),
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
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Запись успешно создана')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Запись на прием'),
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Card(
            margin: const EdgeInsets.all(16),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    widget.doctorName,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 16),
                  InkWell(
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
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Text(
              'Доступное время',
              style: Theme.of(context).textTheme.titleMedium,
            ),
          ),
          const SizedBox(height: 8),
          Expanded(
            child: Consumer<AppointmentProvider>(
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
                          onPressed: _loadSlots,
                          child: const Text('Повторить'),
                        ),
                      ],
                    ),
                  );
                }

                final availableSlots = appointmentProvider.availableSlots
                    .where((slot) => slot.isAvailable)
                    .toList();

                if (availableSlots.isEmpty) {
                  return const Center(
                    child: Text('Нет доступных слотов на эту дату'),
                  );
                }

                return GridView.builder(
                  padding: const EdgeInsets.all(16),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    childAspectRatio: 2,
                    crossAxisSpacing: 8,
                    mainAxisSpacing: 8,
                  ),
                  itemCount: availableSlots.length,
                  itemBuilder: (context, index) {
                    final slot = availableSlots[index];
                    final isSelected = _selectedSlot != null &&
                        DateFormatter.formatTime(_selectedSlot!) ==
                            DateFormatter.formatTime(slot.startTime);

                    return InkWell(
                      onTap: () {
                        setState(() {
                          _selectedSlot = slot.startTime;
                        });
                      },
                      child: Container(
                        decoration: BoxDecoration(
                          color: isSelected
                              ? Theme.of(context).colorScheme.primary
                              : Theme.of(context).colorScheme.surfaceContainerHighest,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        alignment: Alignment.center,
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
                  },
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: ElevatedButton(
              onPressed: _bookAppointment,
              style: ElevatedButton.styleFrom(
                minimumSize: const Size(double.infinity, 48),
              ),
              child: const Text('Записаться'),
            ),
          ),
        ],
      ),
    );
  }
}
