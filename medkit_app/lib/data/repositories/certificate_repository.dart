import 'package:dio/dio.dart';
import '../models/certificate_model.dart';
import '../services/api_service.dart';

class CertificateRepository {
  final ApiService _apiService;

  CertificateRepository(this._apiService);

  Future<List<CertificateModel>> getMyCertificates() async {
    try {
      final response = await _apiService.get('/certificates/patient/my');
      return (response.data as List)
          .map((json) => CertificateModel.fromJson(json))
          .toList();
    } catch (e) {
      throw Exception('Ошибка загрузки справок: $e');
    }
  }

  Future<List<CertificateModel>> getAppointmentCertificates(int appointmentId) async {
    try {
      final response = await _apiService.get('/certificates/appointment/$appointmentId');
      return (response.data as List)
          .map((json) => CertificateModel.fromJson(json))
          .toList();
    } catch (e) {
      throw Exception('Ошибка загрузки справок приема: $e');
    }
  }

  Future<Response> downloadCertificate(int certificateId) async {
    try {
      final response = await _apiService.downloadFile('/certificates/$certificateId/download');
      return response;
    } catch (e) {
      throw Exception('Ошибка скачивания справки: $e');
    }
  }
}
