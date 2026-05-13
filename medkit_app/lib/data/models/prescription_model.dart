class MedicationPrescriptionModel {
  final int idMedicationPrescription;
  final int appointmentId;
  final int medicationId;
  final String medicationName;
  final String? dosage;
  final String? frequency;
  final String? duration;
  final String? instructions;
  final String status;
  final DateTime createdAt;

  MedicationPrescriptionModel({
    required this.idMedicationPrescription,
    required this.appointmentId,
    required this.medicationId,
    required this.medicationName,
    this.dosage,
    this.frequency,
    this.duration,
    this.instructions,
    required this.status,
    required this.createdAt,
  });

  factory MedicationPrescriptionModel.fromJson(Map<String, dynamic> json) {
    return MedicationPrescriptionModel(
      idMedicationPrescription: json['id'],
      appointmentId: json['appointmentId'],
      medicationId: json['medicationId'],
      medicationName: json['medicationName'],
      dosage: json['dosage'],
      frequency: json['frequency'],
      duration: json['duration'],
      instructions: json['instructions'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class ProcedurePrescriptionModel {
  final int idProcedurePrescription;
  final int appointmentId;
  final int procedureId;
  final String procedureName;
  final String? instructions;
  final String status;
  final DateTime createdAt;

  ProcedurePrescriptionModel({
    required this.idProcedurePrescription,
    required this.appointmentId,
    required this.procedureId,
    required this.procedureName,
    this.instructions,
    required this.status,
    required this.createdAt,
  });

  factory ProcedurePrescriptionModel.fromJson(Map<String, dynamic> json) {
    return ProcedurePrescriptionModel(
      idProcedurePrescription: json['id'],
      appointmentId: json['appointmentId'],
      procedureId: json['procedureId'],
      procedureName: json['procedureName'],
      instructions: json['instructions'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class AnalysisPrescriptionModel {
  final int idAnalysisPrescription;
  final int appointmentId;
  final int analysisId;
  final String analysisName;
  final String? instructions;
  final String status;
  final DateTime createdAt;

  AnalysisPrescriptionModel({
    required this.idAnalysisPrescription,
    required this.appointmentId,
    required this.analysisId,
    required this.analysisName,
    this.instructions,
    required this.status,
    required this.createdAt,
  });

  factory AnalysisPrescriptionModel.fromJson(Map<String, dynamic> json) {
    return AnalysisPrescriptionModel(
      idAnalysisPrescription: json['id'],
      appointmentId: json['appointmentId'],
      analysisId: json['analysisId'],
      analysisName: json['analysisName'],
      instructions: json['instructions'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}
