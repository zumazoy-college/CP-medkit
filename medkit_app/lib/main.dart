import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'core/theme/app_theme.dart';
import 'data/services/api_service.dart';
import 'data/services/settings_service.dart';
import 'data/repositories/auth_repository.dart';
import 'data/repositories/doctor_repository.dart';
import 'data/repositories/patient_repository.dart';
import 'data/repositories/appointment_repository.dart';
import 'data/repositories/review_repository.dart';
import 'data/repositories/favorite_repository.dart';
import 'data/repositories/medical_record_repository.dart';
import 'data/repositories/notification_repository.dart';
import 'presentation/providers/auth_provider.dart';
import 'presentation/providers/doctor_provider.dart';
import 'presentation/providers/patient_provider.dart';
import 'presentation/providers/appointment_provider.dart';
import 'presentation/providers/review_provider.dart';
import 'presentation/providers/favorite_provider.dart';
import 'presentation/providers/medical_record_provider.dart';
import 'presentation/providers/notification_provider.dart';
import 'presentation/screens/auth/login_screen.dart';
import 'presentation/screens/home/home_screen.dart';

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final prefs = await SharedPreferences.getInstance();
  final apiService = ApiService(prefs);
  final settingsService = SettingsService(prefs);

  runApp(
    MyApp(
      apiService: apiService,
      settingsService: settingsService,
      isAuthenticated: apiService.isAuthenticated,
    ),
  );
}

class MyApp extends StatelessWidget {
  final ApiService apiService;
  final SettingsService settingsService;
  final bool isAuthenticated;

  const MyApp({
    super.key,
    required this.apiService,
    required this.settingsService,
    required this.isAuthenticated,
  });

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider<ApiService>.value(value: apiService),
        Provider<SettingsService>.value(value: settingsService),

        ProxyProvider<ApiService, AuthRepository>(
          update: (_, apiService, _) => AuthRepository(apiService),
        ),
        ProxyProvider<ApiService, DoctorRepository>(
          update: (_, apiService, _) => DoctorRepository(apiService),
        ),
        ProxyProvider<ApiService, PatientRepository>(
          update: (_, apiService, _) => PatientRepository(apiService),
        ),
        ProxyProvider<ApiService, AppointmentRepository>(
          update: (_, apiService, _) => AppointmentRepository(apiService),
        ),
        ProxyProvider<ApiService, ReviewRepository>(
          update: (_, apiService, _) => ReviewRepository(apiService),
        ),
        ProxyProvider<ApiService, FavoriteRepository>(
          update: (_, apiService, _) => FavoriteRepository(apiService),
        ),
        ProxyProvider<ApiService, MedicalRecordRepository>(
          update: (_, apiService, _) => MedicalRecordRepository(apiService),
        ),
        ProxyProvider<ApiService, NotificationRepository>(
          update: (_, apiService, _) => NotificationRepository(apiService),
        ),

        ChangeNotifierProxyProvider<AuthRepository, AuthProvider>(
          create: (context) => AuthProvider(context.read<AuthRepository>()),
          update: (_, authRepository, previous) =>
              previous ?? AuthProvider(authRepository),
        ),
        ChangeNotifierProxyProvider<DoctorRepository, DoctorProvider>(
          create: (context) => DoctorProvider(context.read<DoctorRepository>()),
          update: (_, doctorRepository, previous) =>
              previous ?? DoctorProvider(doctorRepository),
        ),
        ChangeNotifierProxyProvider<PatientRepository, PatientProvider>(
          create: (context) =>
              PatientProvider(context.read<PatientRepository>()),
          update: (_, patientRepository, previous) =>
              previous ?? PatientProvider(patientRepository),
        ),
        ChangeNotifierProxyProvider<AppointmentRepository, AppointmentProvider>(
          create: (context) =>
              AppointmentProvider(context.read<AppointmentRepository>()),
          update: (_, appointmentRepository, previous) =>
              previous ?? AppointmentProvider(appointmentRepository),
        ),
        ChangeNotifierProxyProvider<ReviewRepository, ReviewProvider>(
          create: (context) => ReviewProvider(context.read<ReviewRepository>()),
          update: (_, reviewRepository, previous) =>
              previous ?? ReviewProvider(reviewRepository),
        ),
        ChangeNotifierProxyProvider<FavoriteRepository, FavoriteProvider>(
          create: (context) =>
              FavoriteProvider(context.read<FavoriteRepository>()),
          update: (_, favoriteRepository, previous) =>
              previous ?? FavoriteProvider(favoriteRepository),
        ),
        ChangeNotifierProxyProvider<
          MedicalRecordRepository,
          MedicalRecordProvider
        >(
          create: (context) =>
              MedicalRecordProvider(context.read<MedicalRecordRepository>()),
          update: (_, medicalRecordRepository, previous) =>
              previous ?? MedicalRecordProvider(medicalRecordRepository),
        ),
        ChangeNotifierProxyProvider<
          NotificationRepository,
          NotificationProvider
        >(
          create: (context) =>
              NotificationProvider(context.read<NotificationRepository>()),
          update: (_, notificationRepository, previous) =>
              previous ?? NotificationProvider(notificationRepository),
        ),
      ],
      child: MaterialApp(
        title: 'MedKit',
        theme: AppTheme.lightTheme,
        debugShowCheckedModeBanner: false,
        navigatorKey: navigatorKey,
        home: isAuthenticated ? const HomeScreen() : const LoginScreen(),
      ),
    );
  }
}
