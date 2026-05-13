import 'package:flutter/foundation.dart';
import '../../data/repositories/auth_repository.dart';

class AuthProvider with ChangeNotifier {
  final AuthRepository _authRepository;

  AuthProvider(this._authRepository);

  bool _isLoading = false;
  String? _error;
  bool _isAuthenticated = false;

  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _isAuthenticated;

  void checkAuthStatus() {
    _isAuthenticated = _authRepository.isAuthenticated;
    notifyListeners();
  }

  Future<bool> register({
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
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.register(
        email: email,
        password: password,
        lastName: lastName,
        firstName: firstName,
        middleName: middleName,
        phoneNumber: phoneNumber,
        dateOfBirth: dateOfBirth,
        gender: gender,
        snils: snils,
      );
      _isAuthenticated = true;
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> login({
    required String email,
    required String password,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.login(email: email, password: password);
      _isAuthenticated = true;
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    await _authRepository.logout();
    _isAuthenticated = false;
    notifyListeners();
  }

  Future<void> sendVerificationCode(String email) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.sendVerificationCode(email);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      rethrow;
    }
  }

  Future<void> verifyEmail(String email, String code) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.verifyEmail(email, code);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      rethrow;
    }
  }

  Future<void> forgotPassword(String email) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.forgotPassword(email);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      rethrow;
    }
  }

  Future<void> verifyResetCode(String email, String code) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.verifyResetCode(email, code);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      rethrow;
    }
  }

  Future<void> resetPassword(String email, String code, String newPassword) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.resetPassword(email, code, newPassword);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      rethrow;
    }
  }

  Future<bool> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authRepository.changePassword(
        oldPassword: oldPassword,
        newPassword: newPassword,
      );
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
