import '../models/diagnosis_model.dart';
import '../models/prescription_model.dart';
import '../models/file_model.dart';
import '../services/api_service.dart';

class MedicalRecordRepository {
  final ApiService _apiService;

  MedicalRecordRepository(this._apiService);

  Future<List<AppointmentDiagnosisModel>> getAppointmentDiagnoses(
      int appointmentId) async {
    final response = await _apiService.get(
      '/diagnoses/appointment/$appointmentId',
    );
    final list = response.data as List;
    return list.map((json) => AppointmentDiagnosisModel.fromJson(json)).toList();
  }

  Future<List<MedicationPrescriptionModel>> getMyMedicationPrescriptions() async {
    final response = await _apiService.get('/prescriptions/medications/my');
    final list = response.data as List;
    return list
        .map((json) => MedicationPrescriptionModel.fromJson(json))
        .toList();
  }

  Future<List<ProcedurePrescriptionModel>> getMyProcedurePrescriptions() async {
    final response = await _apiService.get('/prescriptions/procedures/my');
    final list = response.data as List;
    return list
        .map((json) => ProcedurePrescriptionModel.fromJson(json))
        .toList();
  }

  Future<List<AnalysisPrescriptionModel>> getMyAnalysisPrescriptions() async {
    final response = await _apiService.get('/prescriptions/analyses/my');
    final list = response.data as List;
    return list
        .map((json) => AnalysisPrescriptionModel.fromJson(json))
        .toList();
  }

  Future<List<AppointmentDiagnosisModel>> getMyDiagnoses() async {
    final response = await _apiService.get('/diagnoses/my');
    final list = response.data as List;
    return list
        .map((json) => AppointmentDiagnosisModel.fromJson(json))
        .toList();
  }

  Future<List<FileModel>> getMyFiles() async {
    final response = await _apiService.get('/files/my');
    final list = response.data as List;
    return list.map((json) => FileModel.fromJson(json)).toList();
  }

  Future<List<int>> downloadFile(int fileId) async {
    final response = await _apiService.downloadFile('/files/download/$fileId');
    return response.data as List<int>;
  }
}
