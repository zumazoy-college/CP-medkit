import 'package:flutter/material.dart';
import '../../data/models/certificate_model.dart';
import '../../data/repositories/certificate_repository.dart';

class CertificateProvider with ChangeNotifier {
  final CertificateRepository _repository;

  CertificateProvider(this._repository);

  List<CertificateModel> _certificates = [];
  bool _isLoading = false;
  String? _error;

  List<CertificateModel> get certificates => _certificates;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> loadMyCertificates() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _certificates = await _repository.getMyCertificates();
      _error = null;
    } catch (e) {
      _error = e.toString();
      _certificates = [];
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<List<CertificateModel>> getAppointmentCertificates(int appointmentId) async {
    try {
      return await _repository.getAppointmentCertificates(appointmentId);
    } catch (e) {
      throw Exception('Ошибка загрузки справок: $e');
    }
  }

  Future<List<int>> downloadCertificate(int certificateId) async {
    try {
      final response = await _repository.downloadCertificate(certificateId);
      return response.data as List<int>;
    } catch (e) {
      throw Exception('Ошибка скачивания справки: $e');
    }
  }
}
