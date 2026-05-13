import '../models/patient_model.dart';
import '../services/api_service.dart';
import '../../core/constants/api_constants.dart';
import 'package:dio/dio.dart';

class PatientRepository {
  final ApiService _apiService;

  PatientRepository(this._apiService);

  Future<PatientModel> getMyProfile() async {
    final response = await _apiService.get(ApiConstants.myProfile);
    return PatientModel.fromJson(response.data);
  }

  Future<PatientModel> getPatientById(int id) async {
    final response = await _apiService.get('${ApiConstants.patients}/$id');
    return PatientModel.fromJson(response.data);
  }

  Future<PatientModel> updateMyProfile({
    String? firstName,
    String? lastName,
    String? middleName,
    String? phoneNumber,
    String? allergies,
    String? chronicDiseases,
  }) async {
    final data = <String, dynamic>{};
    if (firstName != null) data['firstName'] = firstName;
    if (lastName != null) data['lastName'] = lastName;
    if (middleName != null) data['middleName'] = middleName;
    if (phoneNumber != null) data['phoneNumber'] = phoneNumber;
    if (allergies != null) data['allergies'] = allergies;
    if (chronicDiseases != null) data['chronicDiseases'] = chronicDiseases;

    final response = await _apiService.put(ApiConstants.myProfile, data: data);
    return PatientModel.fromJson(response.data);
  }

  Future<Map<String, dynamic>> getMySettings() async {
    final response = await _apiService.get('${ApiConstants.myProfile}/settings');
    return response.data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> updateMySettings({
    required bool notifyAppointmentReminder,
    required bool notifyRatingReminder,
    required bool notifyAppointmentCancelled,
    required String defaultScreen,
  }) async {
    final data = {
      'notifyAppointmentReminder': notifyAppointmentReminder,
      'notifyRatingReminder': notifyRatingReminder,
      'notifyAppointmentCancelled': notifyAppointmentCancelled,
      'defaultScreen': defaultScreen,
    };

    final response = await _apiService.put('${ApiConstants.myProfile}/settings', data: data);
    return response.data as Map<String, dynamic>;
  }

  Future<PatientModel> uploadAvatar(String filePath) async {
    final formData = FormData.fromMap({
      'file': await MultipartFile.fromFile(filePath),
    });

    final response = await _apiService.post('${ApiConstants.myProfile}/avatar', data: formData);
    return PatientModel.fromJson(response.data);
  }

  Future<PatientModel> deleteAvatar() async {
    final response = await _apiService.delete('${ApiConstants.myProfile}/avatar');
    return PatientModel.fromJson(response.data);
  }
}
