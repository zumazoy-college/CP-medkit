class FavoriteModel {
  final int idFavorite;
  final int? patientId;
  final int doctorId;
  final String doctorName;
  final String specialization;
  final double rating;
  final String? avatarUrl;
  final DateTime createdAt;
  final String? nextAvailableSlot;

  FavoriteModel({
    required this.idFavorite,
    this.patientId,
    required this.doctorId,
    required this.doctorName,
    required this.specialization,
    required this.rating,
    this.avatarUrl,
    required this.createdAt,
    this.nextAvailableSlot,
  });

  factory FavoriteModel.fromJson(Map<String, dynamic> json) {
    return FavoriteModel(
      idFavorite: json['idFavorite'],
      patientId: json['patientId'],
      doctorId: json['doctorId'],
      doctorName: json['doctorName'],
      specialization: json['specialization'],
      rating: (json['rating'] ?? 0).toDouble(),
      avatarUrl: json['avatarUrl'],
      createdAt: DateTime.parse(json['createdAt']),
      nextAvailableSlot: json['nextAvailableSlot'],
    );
  }

  FavoriteModel copyWith({String? nextAvailableSlot}) {
    return FavoriteModel(
      idFavorite: idFavorite,
      patientId: patientId,
      doctorId: doctorId,
      doctorName: doctorName,
      specialization: specialization,
      rating: rating,
      avatarUrl: avatarUrl,
      createdAt: createdAt,
      nextAvailableSlot: nextAvailableSlot ?? this.nextAvailableSlot,
    );
  }
}
