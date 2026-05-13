class DiagnosisModel {
  final int idDiagnosis;
  final String icdCode;
  final String name;
  final String? description;

  DiagnosisModel({
    required this.idDiagnosis,
    required this.icdCode,
    required this.name,
    this.description,
  });

  factory DiagnosisModel.fromJson(Map<String, dynamic> json) {
    return DiagnosisModel(
      idDiagnosis: json['idDiagnosis'],
      icdCode: json['icdCode'],
      name: json['name'],
      description: json['description'],
    );
  }
}

class AppointmentDiagnosisModel {
  final int idAppointmentDiagnosis;
  final int appointmentId;
  final int diagnosisId;
  final String icdCode;
  final String diagnosisName;
  final bool? isPrimary;
  final String? notes;
  final DateTime appointmentDate;

  AppointmentDiagnosisModel({
    required this.idAppointmentDiagnosis,
    required this.appointmentId,
    required this.diagnosisId,
    required this.icdCode,
    required this.diagnosisName,
    this.isPrimary,
    this.notes,
    required this.appointmentDate,
  });

  factory AppointmentDiagnosisModel.fromJson(Map<String, dynamic> json) {
    return AppointmentDiagnosisModel(
      idAppointmentDiagnosis: json['id'],
      appointmentId: json['appointmentId'],
      diagnosisId: json['diagnosisId'],
      icdCode: json['icdCode'],
      diagnosisName: json['diagnosisName'],
      isPrimary: json['isPrimary'],
      notes: json['notes'],
      appointmentDate: DateTime.parse(json['appointmentDate']),
    );
  }
}
