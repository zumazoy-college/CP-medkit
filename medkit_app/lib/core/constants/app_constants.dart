class AppConstants {
  static const String appName = 'MedKit';
  static const String tokenKey = 'auth_token';
  static const String refreshTokenKey = 'refresh_token';
  static const String userEmailKey = 'user_email';

  // Validation
  static const int minPasswordLength = 8;
  static const int maxPasswordLength = 50;

  // Pagination
  static const int defaultPageSize = 20;

  // Time
  static const int appointmentReminderHours24 = 24;
  static const int appointmentReminderHours2 = 2;
}
