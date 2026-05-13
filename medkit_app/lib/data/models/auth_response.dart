class AuthResponse {
  final String token;
  final String? refreshToken;
  final String? type;
  final int userId;
  final int? doctorId;
  final String email;
  final String role;
  final String firstName;
  final String lastName;
  final String? middleName;

  AuthResponse({
    required this.token,
    this.refreshToken,
    this.type,
    required this.userId,
    this.doctorId,
    required this.email,
    required this.role,
    required this.firstName,
    required this.lastName,
    this.middleName,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'] as String,
      refreshToken: json['refreshToken'] as String?,
      type: json['type'] as String?,
      userId: json['userId'] as int,
      doctorId: json['doctorId'] as int?,
      email: json['email'] as String,
      role: json['role'] as String,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      middleName: json['middleName'] as String?,
    );
  }
}
