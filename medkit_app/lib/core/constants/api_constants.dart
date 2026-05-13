import 'dart:io';
import 'package:flutter/foundation.dart';

class ApiConstants {
  // Environment variable support for production
  static String get baseUrl {
    // Check for environment variable first (for production builds)
    const envApiUrl = String.fromEnvironment('API_URL', defaultValue: '');
    if (envApiUrl.isNotEmpty) {
      return envApiUrl;
    }

    // Development defaults
    if (kIsWeb) {
      return 'http://localhost:8080/api';
    } else if (Platform.isAndroid) {
      return 'http://10.0.2.2:8080/api';
    } else if (Platform.isIOS) {
      return 'http://localhost:8080/api';
    } else {
      return 'http://localhost:8080/api';
    }
  }

  // Auth endpoints
  static const String register = '/auth/register';
  static const String login = '/auth/login';
  static const String refreshToken = '/auth/refresh';
  static const String sendVerificationCode = '/auth/send-verification-code';
  static const String verifyEmail = '/auth/verify-email';
  static const String forgotPassword = '/auth/forgot-password';
  static const String verifyResetCode = '/auth/verify-reset-code';
  static const String resetPassword = '/auth/reset-password';
  static const String changePassword = '/auth/change-password';

  // Doctor endpoints
  static const String doctors = '/doctors';
  static const String doctorSearch = '/doctors/search';
  static const String topRatedDoctors = '/doctors/top-rated';

  // Appointment endpoints
  static const String appointments = '/appointments';
  static const String appointmentSlots = '/appointments/slots';
  static const String bookAppointment = '/appointments/book';
  static const String myAppointments = '/appointments/patient/my';

  // Review endpoints
  static const String reviews = '/reviews';

  // Favorite endpoints
  static const String favorites = '/favorites';
  static const String myFavorites = '/favorites/my';

  // Patient endpoints
  static const String patients = '/patients';
  static const String myProfile = '/patients/me';
}
