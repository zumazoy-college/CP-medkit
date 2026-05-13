class AppointmentModel {
  final int idAppointment;
  final int slotId;
  final int patientId;
  final String patientName;
  final int doctorId;
  final String doctorName;
  final String? doctorSpecialization;
  final String? doctorOffice;
  final DateTime slotDate;
  final DateTime startTime;
  final DateTime endTime;
  final String status;
  final bool? hasReview;
  final int? reviewId;
  final int? reviewRating;
  final String? reviewComment;
  final bool? canEditReview;
  final bool? canDeleteReview;
  final String? primaryDiagnosisName;
  final DateTime createdAt;

  AppointmentModel({
    required this.idAppointment,
    required this.slotId,
    required this.patientId,
    required this.patientName,
    required this.doctorId,
    required this.doctorName,
    this.doctorSpecialization,
    this.doctorOffice,
    required this.slotDate,
    required this.startTime,
    required this.endTime,
    required this.status,
    this.hasReview,
    this.reviewId,
    this.reviewRating,
    this.reviewComment,
    this.canEditReview,
    this.canDeleteReview,
    this.primaryDiagnosisName,
    required this.createdAt,
  });

  factory AppointmentModel.fromJson(Map<String, dynamic> json) {
    return AppointmentModel(
      idAppointment: json['idAppointment'],
      slotId: json['slotId'],
      patientId: json['patientId'],
      patientName: json['patientName'],
      doctorId: json['doctorId'],
      doctorName: json['doctorName'],
      doctorSpecialization: json['doctorSpecialization'],
      doctorOffice: json['doctorOffice'],
      slotDate: DateTime.parse(json['slotDate']),
      startTime: _parseTime(json['startTime']),
      endTime: _parseTime(json['endTime']),
      status: json['status'],
      hasReview: json['hasReview'],
      reviewId: json['reviewId'],
      reviewRating: json['reviewRating'],
      reviewComment: json['reviewComment'],
      canEditReview: json['canEditReview'],
      canDeleteReview: json['canDeleteReview'],
      primaryDiagnosisName: json['primaryDiagnosisName'],
      createdAt: DateTime.parse(json['createdAt']),
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

  bool get isUpcoming {
    final now = DateTime.now();
    final appointmentDateTime = DateTime(
      slotDate.year,
      slotDate.month,
      slotDate.day,
      startTime.hour,
      startTime.minute,
    );
    return appointmentDateTime.isAfter(now) && status != 'cancelled';
  }

  bool get isPast {
    return status == 'completed';
  }
}
