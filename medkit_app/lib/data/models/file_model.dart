class FileModel {
  final int id;
  final int appointmentId;
  final String fileName;
  final DateTime uploadedAt;

  FileModel({
    required this.id,
    required this.appointmentId,
    required this.fileName,
    required this.uploadedAt,
  });

  factory FileModel.fromJson(Map<String, dynamic> json) {
    return FileModel(
      id: json['id'],
      appointmentId: json['appointmentId'],
      fileName: json['fileName'],
      uploadedAt: DateTime.parse(json['uploadedAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'appointmentId': appointmentId,
      'fileName': fileName,
      'uploadedAt': uploadedAt.toIso8601String(),
    };
  }
}
