import 'package:flutter/foundation.dart';
import '../../data/models/diagnosis_model.dart';
import '../../data/models/prescription_model.dart';
import '../../data/models/file_model.dart';
import '../../data/repositories/medical_record_repository.dart';

class MedicalRecordProvider with ChangeNotifier {
  final MedicalRecordRepository _medicalRecordRepository;

  MedicalRecordProvider(this._medicalRecordRepository);

  List<MedicationPrescriptionModel> _medications = [];
  List<ProcedurePrescriptionModel> _procedures = [];
  List<AnalysisPrescriptionModel> _analyses = [];
  List<AppointmentDiagnosisModel> _diagnoses = [];
  List<FileModel> _files = [];
  final Map<int, List<AppointmentDiagnosisModel>> _appointmentDiagnoses = {};

  bool _isLoading = false;
  String? _error;

  List<MedicationPrescriptionModel> get medications => _medications;
  List<ProcedurePrescriptionModel> get procedures => _procedures;
  List<AnalysisPrescriptionModel> get analyses => _analyses;
  List<AppointmentDiagnosisModel> get diagnoses => _diagnoses;
  List<FileModel> get files => _files;
  bool get isLoading => _isLoading;
  String? get error => _error;

  List<AppointmentDiagnosisModel> getDiagnosesForAppointment(int appointmentId) {
    return _appointmentDiagnoses[appointmentId] ?? [];
  }

  Future<void> loadAllPrescriptions() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final results = await Future.wait([
        _medicalRecordRepository.getMyMedicationPrescriptions(),
        _medicalRecordRepository.getMyProcedurePrescriptions(),
        _medicalRecordRepository.getMyAnalysisPrescriptions(),
        _medicalRecordRepository.getMyDiagnoses(),
        _medicalRecordRepository.getMyFiles(),
      ]);

      _medications = results[0] as List<MedicationPrescriptionModel>;
      _procedures = results[1] as List<ProcedurePrescriptionModel>;
      _analyses = results[2] as List<AnalysisPrescriptionModel>;
      _diagnoses = results[3] as List<AppointmentDiagnosisModel>;
      _files = results[4] as List<FileModel>;

      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadAppointmentDiagnoses(int appointmentId) async {
    try {
      final diagnoses = await _medicalRecordRepository
          .getAppointmentDiagnoses(appointmentId);
      _appointmentDiagnoses[appointmentId] = diagnoses;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<List<int>> downloadFile(int fileId) async {
    try {
      return await _medicalRecordRepository.downloadFile(fileId);
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      rethrow;
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
