import 'package:shared_preferences/shared_preferences.dart';

class SettingsService {
  final SharedPreferences _prefs;

  SettingsService(this._prefs);

  // Keys
  static const String _defaultScreenKey = 'default_screen';
  static const String _notifyAppointmentReminderKey = 'notify_appointment_reminder';
  static const String _notifyAppointmentCancelledKey = 'notify_appointment_cancelled';
  static const String _notifyAnalysisReadyKey = 'notify_analysis_ready';
  static const String _notifyNewReferralKey = 'notify_new_referral';

  // Default screen
  Future<void> setDefaultScreen(String screen) async {
    await _prefs.setString(_defaultScreenKey, screen);
  }

  String getDefaultScreen() {
    return _prefs.getString(_defaultScreenKey) ?? 'search';
  }

  // Notification settings
  Future<void> setNotifyAppointmentReminder(bool value) async {
    await _prefs.setBool(_notifyAppointmentReminderKey, value);
  }

  bool getNotifyAppointmentReminder() {
    return _prefs.getBool(_notifyAppointmentReminderKey) ?? true;
  }

  Future<void> setNotifyAppointmentCancelled(bool value) async {
    await _prefs.setBool(_notifyAppointmentCancelledKey, value);
  }

  bool getNotifyAppointmentCancelled() {
    return _prefs.getBool(_notifyAppointmentCancelledKey) ?? true;
  }

  Future<void> setNotifyAnalysisReady(bool value) async {
    await _prefs.setBool(_notifyAnalysisReadyKey, value);
  }

  bool getNotifyAnalysisReady() {
    return _prefs.getBool(_notifyAnalysisReadyKey) ?? true;
  }

  Future<void> setNotifyNewReferral(bool value) async {
    await _prefs.setBool(_notifyNewReferralKey, value);
  }

  bool getNotifyNewReferral() {
    return _prefs.getBool(_notifyNewReferralKey) ?? true;
  }
}
