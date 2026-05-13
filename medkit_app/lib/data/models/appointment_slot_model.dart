class AppointmentSlotModel {
  final int idSlot;
  final int doctorId;
  final DateTime slotDate;
  final DateTime startTime;
  final DateTime endTime;
  final String status;
  final int? appointmentId;

  AppointmentSlotModel({
    required this.idSlot,
    required this.doctorId,
    required this.slotDate,
    required this.startTime,
    required this.endTime,
    required this.status,
    this.appointmentId,
  });

  factory AppointmentSlotModel.fromJson(Map<String, dynamic> json) {
    return AppointmentSlotModel(
      idSlot: json['idSlot'],
      doctorId: json['doctorId'],
      slotDate: DateTime.parse(json['slotDate']),
      startTime: _parseTime(json['startTime']),
      endTime: _parseTime(json['endTime']),
      status: json['status'],
      appointmentId: json['appointmentId'],
    );
  }

  static DateTime _parseTime(String timeString) {
    final parts = timeString.split(':');
    final now = DateTime.now();
    return DateTime(
      now.year,
      now.month,
      now.day,
      int.parse(parts[0]),
      int.parse(parts[1]),
    );
  }

  bool get isAvailable => status.toLowerCase() == 'free';
}
