class PatientModel {
  final int idPatient;
  final int userId;
  final String email;
  final String lastName;
  final String firstName;
  final String? middleName;
  final String phoneNumber;
  final DateTime dateOfBirth;
  final String gender;
  final String snils;
  final String? avatarUrl;
  final String? allergies;
  final String? chronicDiseases;

  PatientModel({
    required this.idPatient,
    required this.userId,
    required this.email,
    required this.lastName,
    required this.firstName,
    this.middleName,
    required this.phoneNumber,
    required this.dateOfBirth,
    required this.gender,
    required this.snils,
    this.avatarUrl,
    this.allergies,
    this.chronicDiseases,
  });

  factory PatientModel.fromJson(Map<String, dynamic> json) {
    return PatientModel(
      idPatient: json['idPatient'],
      userId: json['userId'],
      email: json['email'],
      lastName: json['lastName'],
      firstName: json['firstName'],
      middleName: json['middleName'],
      phoneNumber: json['phoneNumber'],
      dateOfBirth: DateTime.parse(json['dateOfBirth']),
      gender: json['gender'],
      snils: json['snils'],
      avatarUrl: json['avatarUrl'],
      allergies: json['allergies'],
      chronicDiseases: json['chronicDiseases'],
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

  int get age {
    final now = DateTime.now();
    int age = now.year - dateOfBirth.year;
    if (now.month < dateOfBirth.month ||
        (now.month == dateOfBirth.month && now.day < dateOfBirth.day)) {
      age--;
    }
    return age;
  }
}
