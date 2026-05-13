class CertificateModel {
  final int idCertificate;
  final int appointmentId;
  final int patientId;
  final String patientFullName;
  final int doctorId;
  final String doctorFullName;
  final String doctorSpecialization;
  final String certificateType;
  final String certificateTypeName;
  final String filePath;
  final DateTime? validFrom;
  final DateTime? validTo;
  final DateTime? disabilityPeriodFrom;
  final DateTime? disabilityPeriodTo;
  final String? workRestrictions;
  final DateTime createdAt;

  CertificateModel({
    required this.idCertificate,
    required this.appointmentId,
    required this.patientId,
    required this.patientFullName,
    required this.doctorId,
    required this.doctorFullName,
    required this.doctorSpecialization,
    required this.certificateType,
    required this.certificateTypeName,
    required this.filePath,
    this.validFrom,
    this.validTo,
    this.disabilityPeriodFrom,
    this.disabilityPeriodTo,
    this.workRestrictions,
    required this.createdAt,
  });

  factory CertificateModel.fromJson(Map<String, dynamic> json) {
    return CertificateModel(
      idCertificate: json['idCertificate'],
      appointmentId: json['appointmentId'],
      patientId: json['patientId'],
      patientFullName: json['patientFullName'],
      doctorId: json['doctorId'],
      doctorFullName: json['doctorFullName'],
      doctorSpecialization: json['doctorSpecialization'],
      certificateType: json['certificateType'],
      certificateTypeName: json['certificateTypeName'],
      filePath: json['filePath'],
      validFrom: json['validFrom'] != null ? DateTime.parse(json['validFrom']) : null,
      validTo: json['validTo'] != null ? DateTime.parse(json['validTo']) : null,
      disabilityPeriodFrom: json['disabilityPeriodFrom'] != null
          ? DateTime.parse(json['disabilityPeriodFrom'])
          : null,
      disabilityPeriodTo: json['disabilityPeriodTo'] != null
          ? DateTime.parse(json['disabilityPeriodTo'])
          : null,
      workRestrictions: json['workRestrictions'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}
