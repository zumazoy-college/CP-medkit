import '../models/doctor_model.dart';
import '../services/api_service.dart';
import '../../core/constants/api_constants.dart';

class DoctorRepository {
  final ApiService _apiService;

  DoctorRepository(this._apiService);

  Future<List<DoctorModel>> getAllDoctors({
    int page = 0,
    int size = 20,
  }) async {
    final response = await _apiService.get(
      ApiConstants.doctors,
      queryParameters: {
        'page': page,
        'size': size,
      },
    );

    final content = response.data['content'] as List;
    return content.map((json) => DoctorModel.fromJson(json)).toList();
  }

  Future<DoctorModel> getDoctorById(int id) async {
    final response = await _apiService.get('${ApiConstants.doctors}/$id');
    return DoctorModel.fromJson(response.data);
  }

  Future<List<DoctorModel>> searchDoctors({
    String? specialization,
    double? minRating,
    String? name,
    String? gender,
    int? minExperience,
    List<String>? sort,
    int page = 0,
    int size = 20,
  }) async {
    final queryParams = <String, dynamic>{
      'page': page,
      'size': size,
    };

    if (specialization != null && specialization.isNotEmpty) {
      queryParams['specialization'] = specialization;
    }
    if (minRating != null) {
      queryParams['minRating'] = minRating;
    }
    if (name != null && name.isNotEmpty) {
      queryParams['name'] = name;
    }
    if (gender != null && gender.isNotEmpty) {
      queryParams['gender'] = gender;
    }
    if (minExperience != null) {
      queryParams['minExperience'] = minExperience;
    }
    if (sort != null && sort.isNotEmpty) {
      queryParams['sort'] = sort;
    }

    final response = await _apiService.get(
      ApiConstants.doctorSearch,
      queryParameters: queryParams,
    );

    final content = response.data['content'] as List;
    return content.map((json) => DoctorModel.fromJson(json)).toList();
  }

  Future<List<DoctorModel>> getTopRatedDoctors() async {
    final response = await _apiService.get(ApiConstants.topRatedDoctors);
    final list = response.data as List;
    return list.map((json) => DoctorModel.fromJson(json)).toList();
  }
}
