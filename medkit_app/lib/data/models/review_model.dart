class ReviewModel {
  final int idReview;
  final int patientId;
  final String patientName;
  final int doctorId;
  final int appointmentId;
  final int rating;
  final String? comment;
  final DateTime createdAt;
  final bool canDelete;
  final bool canEdit;

  ReviewModel({
    required this.idReview,
    required this.patientId,
    required this.patientName,
    required this.doctorId,
    required this.appointmentId,
    required this.rating,
    this.comment,
    required this.createdAt,
    required this.canDelete,
    required this.canEdit,
  });

  factory ReviewModel.fromJson(Map<String, dynamic> json) {
    return ReviewModel(
      idReview: json['idReview'],
      patientId: json['patientId'],
      patientName: json['patientName'],
      doctorId: json['doctorId'],
      appointmentId: json['appointmentId'],
      rating: json['rating'],
      comment: json['comment'],
      createdAt: DateTime.parse(json['createdAt']),
      canDelete: json['canDelete'] ?? false,
      canEdit: json['canEdit'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'appointmentId': appointmentId,
      'rating': rating,
      'comment': comment,
    };
  }
}
