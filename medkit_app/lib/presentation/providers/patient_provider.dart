import 'package:flutter/foundation.dart';
import '../../data/models/patient_model.dart';
import '../../data/repositories/patient_repository.dart';

class PatientProvider with ChangeNotifier {
  final PatientRepository _patientRepository;

  PatientProvider(this._patientRepository);

  PatientModel? _currentPatient;
  bool _isLoading = false;
  String? _error;

  PatientModel? get currentPatient => _currentPatient;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> loadMyProfile() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentPatient = await _patientRepository.getMyProfile();
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateMyProfile({
    String? firstName,
    String? lastName,
    String? middleName,
    String? phoneNumber,
    String? allergies,
    String? chronicDiseases,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentPatient = await _patientRepository.updateMyProfile(
        firstName: firstName,
        lastName: lastName,
        middleName: middleName,
        phoneNumber: phoneNumber,
        allergies: allergies,
        chronicDiseases: chronicDiseases,
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

  Future<Map<String, dynamic>?> getMySettings() async {
    try {
      return await _patientRepository.getMySettings();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return null;
    }
  }

  Future<bool> updateMySettings({
    required bool notifyAppointmentReminder,
    required bool notifyRatingReminder,
    required bool notifyAppointmentCancelled,
    required String defaultScreen,
  }) async {
    try {
      await _patientRepository.updateMySettings(
        notifyAppointmentReminder: notifyAppointmentReminder,
        notifyRatingReminder: notifyRatingReminder,
        notifyAppointmentCancelled: notifyAppointmentCancelled,
        defaultScreen: defaultScreen,
      );
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    }
  }

  Future<bool> uploadAvatar(String filePath) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentPatient = await _patientRepository.uploadAvatar(filePath);
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

  Future<bool> deleteAvatar() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentPatient = await _patientRepository.deleteAvatar();
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

  void clearProfile() {
    _currentPatient = null;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
