import '../models/auth_response.dart';
import '../services/api_service.dart';
import '../../core/constants/api_constants.dart';

class AuthRepository {
  final ApiService _apiService;

  AuthRepository(this._apiService);

  Future<AuthResponse> register({
    required String email,
    required String password,
    required String lastName,
    required String firstName,
    String? middleName,
    required String phoneNumber,
    required DateTime dateOfBirth,
    required String gender,
    required String snils,
  }) async {
    final response = await _apiService.post(
      ApiConstants.register,
      data: {
        'email': email,
        'password': password,
        'lastName': lastName,
        'firstName': firstName,
        'middleName': middleName,
        'phoneNumber': phoneNumber.replaceAll(RegExp(r'[^\d]'), ''), // Только цифры
        'birthdate': dateOfBirth.toIso8601String().split('T')[0], // Изменено с dateOfBirth на birthdate
        'gender': gender.toLowerCase(), // Конвертируем в lowercase (MALE -> male)
        'snils': snils.replaceAll(RegExp(r'[^\d]'), ''), // Только цифры, убираем дефисы
      },
    );

    final authResponse = AuthResponse.fromJson(response.data);
    await _apiService.saveToken(authResponse.token);
    if (authResponse.refreshToken != null) {
      await _apiService.saveRefreshToken(authResponse.refreshToken!);
    }
    await _apiService.saveUserEmail(authResponse.email);
    return authResponse;
  }

  Future<AuthResponse> login({
    required String email,
    required String password,
  }) async {
    final response = await _apiService.post(
      ApiConstants.login,
      data: {
        'email': email,
        'password': password,
        'expectedRole': 'patient', // Указываем, что ожидаем роль пациента
      },
    );

    final authResponse = AuthResponse.fromJson(response.data);

    // Дополнительная проверка роли на клиенте
    if (authResponse.role.toLowerCase() != 'patient') {
      throw Exception('Доступ запрещен. Используйте веб-версию для входа как врач.');
    }

    await _apiService.saveToken(authResponse.token);
    await _apiService.saveUserEmail(authResponse.email);
    return authResponse;
  }

  Future<void> sendVerificationCode(String email) async {
    await _apiService.post(
      ApiConstants.sendVerificationCode,
      data: {'email': email},
    );
  }

  Future<void> verifyEmail(String email, String code) async {
    await _apiService.post(
      ApiConstants.verifyEmail,
      data: {
        'email': email,
        'code': code,
      },
    );
  }

  Future<void> forgotPassword(String email) async {
    await _apiService.post(
      ApiConstants.forgotPassword,
      data: {'email': email},
    );
  }

  Future<void> verifyResetCode(String email, String code) async {
    await _apiService.post(
      ApiConstants.verifyResetCode,
      data: {
        'email': email,
        'code': code,
      },
    );
  }

  Future<void> resetPassword(String email, String code, String newPassword) async {
    await _apiService.post(
      ApiConstants.resetPassword,
      data: {
        'email': email,
        'code': code,
        'newPassword': newPassword,
      },
    );
  }

  Future<void> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    await _apiService.post(
      ApiConstants.changePassword,
      data: {
        'old_password': oldPassword,
        'new_password': newPassword,
      },
    );
  }

  Future<void> logout() async {
    await _apiService.clearAuth();
  }

  bool get isAuthenticated => _apiService.isAuthenticated;
}
