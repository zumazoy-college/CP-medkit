class DetailedAppointmentModel {
  final int idAppointment;
  final int slotId;
  final PatientInfo patient;
  final DoctorInfo doctor;
  final DateTime slotDate;
  final DateTime startTime;
  final DateTime endTime;
  final String status;
  final String? complaints;
  final String? anamnesis;
  final String? objectiveData;
  final String? recommendations;
  final List<DetailedDiagnosisModel> diagnoses;
  final List<DetailedMedicationModel> medications;
  final List<DetailedProcedureModel> procedures;
  final List<DetailedAnalysisModel> analyses;
  final List<DetailedFileModel> files;
  final DateTime createdAt;

  DetailedAppointmentModel({
    required this.idAppointment,
    required this.slotId,
    required this.patient,
    required this.doctor,
    required this.slotDate,
    required this.startTime,
    required this.endTime,
    required this.status,
    this.complaints,
    this.anamnesis,
    this.objectiveData,
    this.recommendations,
    required this.diagnoses,
    required this.medications,
    required this.procedures,
    required this.analyses,
    required this.files,
    required this.createdAt,
  });

  factory DetailedAppointmentModel.fromJson(Map<String, dynamic> json) {
    return DetailedAppointmentModel(
      idAppointment: json['idAppointment'],
      slotId: json['slotId'],
      patient: PatientInfo.fromJson(json['patient']),
      doctor: DoctorInfo.fromJson(json['doctor']),
      slotDate: DateTime.parse(json['slotDate']),
      startTime: _parseTime(json['startTime']),
      endTime: _parseTime(json['endTime']),
      status: json['status'],
      complaints: json['complaints'],
      anamnesis: json['anamnesis'],
      objectiveData: json['objectiveData'],
      recommendations: json['recommendations'],
      diagnoses: (json['diagnoses'] as List?)
              ?.map((d) => DetailedDiagnosisModel.fromJson(d))
              .toList() ??
          [],
      medications: (json['medications'] as List?)
              ?.map((m) => DetailedMedicationModel.fromJson(m))
              .toList() ??
          [],
      procedures: (json['procedures'] as List?)
              ?.map((p) => DetailedProcedureModel.fromJson(p))
              .toList() ??
          [],
      analyses: (json['analyses'] as List?)
              ?.map((a) => DetailedAnalysisModel.fromJson(a))
              .toList() ??
          [],
      files: (json['files'] as List?)
              ?.map((f) => DetailedFileModel.fromJson(f))
              .toList() ??
          [],
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
}

class DetailedDiagnosisModel {
  final int id;
  final int appointmentId;
  final int diagnosisId;
  final String diagnosisName;
  final String icdCode;
  final bool? isPrimary;
  final String? notes;

  DetailedDiagnosisModel({
    required this.id,
    required this.appointmentId,
    required this.diagnosisId,
    required this.diagnosisName,
    required this.icdCode,
    this.isPrimary,
    this.notes,
  });

  factory DetailedDiagnosisModel.fromJson(Map<String, dynamic> json) {
    return DetailedDiagnosisModel(
      id: json['id'],
      appointmentId: json['appointmentId'],
      diagnosisId: json['diagnosisId'],
      diagnosisName: json['diagnosisName'],
      icdCode: json['icdCode'],
      isPrimary: json['isPrimary'],
      notes: json['notes'],
    );
  }
}

class DetailedMedicationModel {
  final int id;
  final int appointmentId;
  final int medicationId;
  final String medicationName;
  final String? dosage;
  final String? frequency;
  final String? duration;
  final String? instructions;
  final String status;
  final DateTime createdAt;

  DetailedMedicationModel({
    required this.id,
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

  factory DetailedMedicationModel.fromJson(Map<String, dynamic> json) {
    return DetailedMedicationModel(
      id: json['id'],
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

class DetailedProcedureModel {
  final int id;
  final int appointmentId;
  final int procedureId;
  final String procedureName;
  final String? instructions;
  final String status;
  final DateTime createdAt;

  DetailedProcedureModel({
    required this.id,
    required this.appointmentId,
    required this.procedureId,
    required this.procedureName,
    this.instructions,
    required this.status,
    required this.createdAt,
  });

  factory DetailedProcedureModel.fromJson(Map<String, dynamic> json) {
    return DetailedProcedureModel(
      id: json['id'],
      appointmentId: json['appointmentId'],
      procedureId: json['procedureId'],
      procedureName: json['procedureName'],
      instructions: json['instructions'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class DetailedAnalysisModel {
  final int id;
  final int appointmentId;
  final int analysisId;
  final String analysisName;
  final String? instructions;
  final String status;
  final DateTime createdAt;

  DetailedAnalysisModel({
    required this.id,
    required this.appointmentId,
    required this.analysisId,
    required this.analysisName,
    this.instructions,
    required this.status,
    required this.createdAt,
  });

  factory DetailedAnalysisModel.fromJson(Map<String, dynamic> json) {
    return DetailedAnalysisModel(
      id: json['id'],
      appointmentId: json['appointmentId'],
      analysisId: json['analysisId'],
      analysisName: json['analysisName'],
      instructions: json['instructions'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class DetailedFileModel {
  final int id;
  final int appointmentId;
  final String fileName;
  final DateTime uploadedAt;

  DetailedFileModel({
    required this.id,
    required this.appointmentId,
    required this.fileName,
    required this.uploadedAt,
  });

  factory DetailedFileModel.fromJson(Map<String, dynamic> json) {
    return DetailedFileModel(
      id: json['id'],
      appointmentId: json['appointmentId'],
      fileName: json['fileName'],
      uploadedAt: DateTime.parse(json['uploadedAt']),
    );
  }
}

class PatientInfo {
  final int id;
  final String firstName;
  final String lastName;
  final String? middleName;
  final DateTime dateOfBirth;
  final String snils;

  PatientInfo({
    required this.id,
    required this.firstName,
    required this.lastName,
    this.middleName,
    required this.dateOfBirth,
    required this.snils,
  });

  factory PatientInfo.fromJson(Map<String, dynamic> json) {
    return PatientInfo(
      id: json['id'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      middleName: json['middleName'],
      dateOfBirth: DateTime.parse(json['dateOfBirth']),
      snils: json['snils'],
    );
  }

  String get fullName {
    if (middleName != null) {
      return '$lastName $firstName $middleName';
    }
    return '$lastName $firstName';
  }
}

class DoctorInfo {
  final int id;
  final String firstName;
  final String lastName;
  final String? middleName;
  final String specialization;

  DoctorInfo({
    required this.id,
    required this.firstName,
    required this.lastName,
    this.middleName,
    required this.specialization,
  });

  factory DoctorInfo.fromJson(Map<String, dynamic> json) {
    return DoctorInfo(
      id: json['id'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      middleName: json['middleName'],
      specialization: json['specialization'],
    );
  }

  String get fullName {
    if (middleName != null) {
      return '$lastName $firstName $middleName';
    }
    return '$lastName $firstName';
  }
}
