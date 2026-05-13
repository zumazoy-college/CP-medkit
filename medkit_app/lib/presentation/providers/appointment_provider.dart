import 'package:flutter/foundation.dart';
import '../../data/models/appointment_model.dart';
import '../../data/models/appointment_slot_model.dart';
import '../../data/models/detailed_appointment_model.dart';
import '../../data/repositories/appointment_repository.dart';

class AppointmentProvider with ChangeNotifier {
  final AppointmentRepository _appointmentRepository;

  AppointmentProvider(this._appointmentRepository);

  List<AppointmentSlotModel> _availableSlots = [];
  List<AppointmentModel> _myAppointments = [];
  DetailedAppointmentModel? _detailedAppointment;
  bool _isLoading = false;
  String? _error;
  String? _successMessage;

  List<AppointmentSlotModel> get availableSlots => _availableSlots;
  List<AppointmentModel> get myAppointments => _myAppointments;
  DetailedAppointmentModel? get detailedAppointment => _detailedAppointment;
  List<AppointmentModel> get upcomingAppointments =>
      _myAppointments.where((a) => a.isUpcoming).toList();
  List<AppointmentModel> get pastAppointments =>
      _myAppointments.where((a) => a.isPast).toList();
  bool get isLoading => _isLoading;
  String? get error => _error;
  String? get successMessage => _successMessage;

  Future<void> loadAvailableSlots({
    required int doctorId,
    required DateTime date,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _availableSlots = await _appointmentRepository.getAvailableSlots(
        doctorId: doctorId,
        date: date,
      );
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> bookAppointment({
    required int doctorId,
    required DateTime slotDate,
    required DateTime startTime,
  }) async {
    _isLoading = true;
    _error = null;
    _successMessage = null;
    notifyListeners();

    try {
      await _appointmentRepository.bookAppointment(
        doctorId: doctorId,
        slotDate: slotDate,
        startTime: startTime,
      );
      _successMessage = 'Запись успешно создана';
      _isLoading = false;
      notifyListeners();
      await loadMyAppointments();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> cancelAppointment({
    required int slotId,
    required String reason,
  }) async {
    _isLoading = true;
    _error = null;
    _successMessage = null;
    notifyListeners();

    try {
      await _appointmentRepository.cancelAppointment(
        slotId: slotId,
        reason: reason,
      );
      _successMessage = 'Запись успешно отменена';
      _isLoading = false;
      notifyListeners();
      await loadMyAppointments();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> loadMyAppointments() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _myAppointments = await _appointmentRepository.getMyAppointments();
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadAppointmentDetails(int appointmentId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _detailedAppointment = await _appointmentRepository.getAppointmentById(appointmentId);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  void clearMessages() {
    _error = null;
    _successMessage = null;
    notifyListeners();
  }
}
