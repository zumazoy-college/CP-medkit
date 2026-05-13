class DoctorModel {
  final int idDoctor;
  final int userId;
  final String email;
  final String lastName;
  final String firstName;
  final String? middleName;
  final String? phoneNumber;
  final String? avatarUrl;
  final String specialization;
  final double rating;
  final int reviewsCount;
  final String? office;
  final String? workExperience;
  final int experienceYears;
  final String? gender;
  final String? nextAvailableSlot;

  DoctorModel({
    required this.idDoctor,
    required this.userId,
    required this.email,
    required this.lastName,
    required this.firstName,
    this.middleName,
    this.phoneNumber,
    this.avatarUrl,
    required this.specialization,
    required this.rating,
    required this.reviewsCount,
    this.office,
    this.workExperience,
    required this.experienceYears,
    this.gender,
    this.nextAvailableSlot,
  });

  factory DoctorModel.fromJson(Map<String, dynamic> json) {
    return DoctorModel(
      idDoctor: json['idDoctor'],
      userId: json['userId'],
      email: json['email'],
      lastName: json['lastName'],
      firstName: json['firstName'],
      middleName: json['middleName'],
      phoneNumber: json['phoneNumber'],
      avatarUrl: json['avatarUrl'],
      specialization: json['specialization'],
      rating: (json['rating'] ?? 0).toDouble(),
      reviewsCount: json['reviewsCount'] ?? 0,
      office: json['office'],
      workExperience: json['workExperience'],
      experienceYears: json['experienceYears'] ?? 0,
      gender: json['gender'],
      nextAvailableSlot: json['nextAvailableSlot'],
    );
  }

  String get fullName {
    if (middleName != null && middleName!.isNotEmpty) {
      return '$lastName $firstName $middleName';
    }
    return '$lastName $firstName';
  }

  String get shortName {
    final middleInitial = middleName != null && middleName!.isNotEmpty
        ? ' ${middleName![0]}.'
        : '';
    return '$lastName ${firstName[0]}.$middleInitial';
  }
}
