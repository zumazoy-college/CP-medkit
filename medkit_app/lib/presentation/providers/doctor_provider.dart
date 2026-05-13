import 'package:flutter/foundation.dart';
import '../../data/models/doctor_model.dart';
import '../../data/repositories/doctor_repository.dart';

class DoctorProvider with ChangeNotifier {
  final DoctorRepository _doctorRepository;

  DoctorProvider(this._doctorRepository);

  List<DoctorModel> _doctors = [];
  DoctorModel? _selectedDoctor;
  bool _isLoading = false;
  String? _error;

  List<DoctorModel> get doctors => _doctors;
  DoctorModel? get selectedDoctor => _selectedDoctor;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> loadDoctors() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _doctors = await _doctorRepository.getAllDoctors();
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> searchDoctors({
    String? specialization,
    double? minRating,
    String? name,
    String? gender,
    int? minExperience,
    List<String>? sort,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _doctors = await _doctorRepository.searchDoctors(
        specialization: specialization,
        minRating: minRating,
        name: name,
        gender: gender,
        minExperience: minExperience,
        sort: sort,
      );
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadDoctorById(int id) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _selectedDoctor = await _doctorRepository.getDoctorById(id);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  void clearSelectedDoctor() {
    _selectedDoctor = null;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
