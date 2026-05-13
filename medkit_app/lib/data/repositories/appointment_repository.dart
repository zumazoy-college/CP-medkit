import '../models/appointment_model.dart';
import '../models/appointment_slot_model.dart';
import '../models/detailed_appointment_model.dart';
import '../services/api_service.dart';
import '../../core/constants/api_constants.dart';
import '../../core/utils/date_formatter.dart';

class AppointmentRepository {
  final ApiService _apiService;

  AppointmentRepository(this._apiService);

  Future<List<AppointmentSlotModel>> getAvailableSlots({
    required int doctorId,
    required DateTime date,
  }) async {
    final response = await _apiService.get(
      ApiConstants.appointmentSlots,
      queryParameters: {
        'doctorId': doctorId,
        'date': DateFormatter.formatDateForApi(date),
      },
    );

    final list = response.data as List;
    return list.map((json) => AppointmentSlotModel.fromJson(json)).toList();
  }

  Future<AppointmentSlotModel> bookAppointment({
    required int doctorId,
    required DateTime slotDate,
    required DateTime startTime,
  }) async {
    final response = await _apiService.post(
      ApiConstants.bookAppointment,
      queryParameters: {
        'doctorId': doctorId,
        'slotDate': DateFormatter.formatDateForApi(slotDate),
        'startTime': DateFormatter.formatTimeForApi(startTime),
      },
    );

    return AppointmentSlotModel.fromJson(response.data);
  }

  Future<void> cancelAppointment({
    required int slotId,
    required String reason,
  }) async {
    await _apiService.post(
      '${ApiConstants.appointments}/$slotId/cancel',
      queryParameters: {'reason': reason},
    );
  }

  Future<List<AppointmentModel>> getMyAppointments({
    int page = 0,
    int size = 20,
  }) async {
    final response = await _apiService.get(
      ApiConstants.myAppointments,
      queryParameters: {
        'page': page,
        'size': size,
      },
    );

    final content = response.data['content'] as List;
    return content.map((json) => AppointmentModel.fromJson(json)).toList();
  }

  Future<DetailedAppointmentModel> getAppointmentById(int appointmentId) async {
    final response = await _apiService.get(
      '${ApiConstants.appointments}/$appointmentId',
    );

    return DetailedAppointmentModel.fromJson(response.data);
  }

  Future<DateTime?> findNextAvailableSlot({
    required int doctorId,
    int daysToCheck = 30,
  }) async {
    final today = DateTime.now();

    for (int i = 0; i < daysToCheck; i++) {
      final checkDate = today.add(Duration(days: i));
      try {
        final slots = await getAvailableSlots(
          doctorId: doctorId,
          date: checkDate,
        );

        if (slots.isNotEmpty) {
          return checkDate;
        }
      } catch (e) {
        // Continue checking next day if this day fails
        continue;
      }
    }

    return null; // No slots found in the next daysToCheck days
  }
}
