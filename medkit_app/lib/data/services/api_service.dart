import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/api_constants.dart';
import '../../core/constants/app_constants.dart';
import '../../main.dart';
import '../../presentation/screens/auth/login_screen.dart';

class ApiService {
  late final Dio _dio;
  final SharedPreferences _prefs;
  bool _isRefreshing = false;
  final List<Function> _refreshQueue = [];

  ApiService(this._prefs) {
    _dio = Dio(
      BaseOptions(
        baseUrl: ApiConstants.baseUrl,
        connectTimeout: const Duration(seconds: 30),
        receiveTimeout: const Duration(seconds: 30),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = _prefs.getString(AppConstants.tokenKey);
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          return handler.next(options);
        },
        onError: (error, handler) async {
          if (error.response?.statusCode == 401) {
            // Try to refresh token
            final refreshToken = _prefs.getString(AppConstants.refreshTokenKey);

            if (refreshToken != null && !_isRefreshing) {
              _isRefreshing = true;

              try {
                // Attempt to refresh the token
                final response = await _dio.post(
                  ApiConstants.refreshToken,
                  data: {'refreshToken': refreshToken},
                  options: Options(
                    headers: {
                      'Content-Type': 'application/json',
                      'Accept': 'application/json',
                    },
                  ),
                );

                final newToken = response.data['token'] as String;
                final newRefreshToken = response.data['refreshToken'] as String;

                await _prefs.setString(AppConstants.tokenKey, newToken);
                await _prefs.setString(AppConstants.refreshTokenKey, newRefreshToken);

                _isRefreshing = false;

                // Retry the original request with new token
                error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
                final retryResponse = await _dio.fetch(error.requestOptions);

                // Process queued requests
                for (var callback in _refreshQueue) {
                  callback();
                }
                _refreshQueue.clear();

                return handler.resolve(retryResponse);
              } catch (e) {
                _isRefreshing = false;
                _refreshQueue.clear();

                // Refresh failed, clear auth and notify user
                await _prefs.remove(AppConstants.tokenKey);
                await _prefs.remove(AppConstants.refreshTokenKey);
                await _prefs.remove(AppConstants.userEmailKey);

                // Show notification and redirect to login
                _handleAuthFailure();

                return handler.next(error);
              }
            } else if (_isRefreshing) {
              // Queue this request while refresh is in progress
              return handler.next(error);
            } else {
              // No refresh token available
              await _prefs.remove(AppConstants.tokenKey);
              await _prefs.remove(AppConstants.refreshTokenKey);
              await _prefs.remove(AppConstants.userEmailKey);
            }
          }
          return handler.next(error);
        },
      ),
    );
  }

  Future<Response> get(String path, {Map<String, dynamic>? queryParameters}) async {
    try {
      return await _dio.get(path, queryParameters: queryParameters);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> post(String path, {dynamic data, Map<String, dynamic>? queryParameters}) async {
    try {
      return await _dio.post(path, data: data, queryParameters: queryParameters);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> put(String path, {dynamic data}) async {
    try {
      return await _dio.put(path, data: data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> delete(String path, {Map<String, dynamic>? queryParameters}) async {
    try {
      return await _dio.delete(path, queryParameters: queryParameters);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> downloadFile(String path) async {
    try {
      return await _dio.get(
        path,
        options: Options(
          responseType: ResponseType.bytes,
          followRedirects: false,
          validateStatus: (status) => status! < 500,
        ),
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  String _handleError(DioException error) {
    if (error.response != null) {
      final data = error.response!.data;
      if (data is Map && data.containsKey('message')) {
        return data['message'];
      }
      return 'Ошибка сервера: ${error.response!.statusCode}';
    } else if (error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout) {
      return 'Превышено время ожидания';
    } else if (error.type == DioExceptionType.connectionError) {
      return 'Ошибка подключения к серверу';
    }
    return 'Произошла ошибка: ${error.message}';
  }

  void _handleAuthFailure() {
    final context = navigatorKey.currentContext;
    if (context != null) {
      // Show snackbar notification
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Сессия истекла. Пожалуйста, войдите снова.'),
          backgroundColor: Colors.red,
          duration: Duration(seconds: 3),
        ),
      );

      // Navigate to login screen
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (context) => const LoginScreen()),
        (route) => false,
      );
    }
  }

  Future<void> saveToken(String token) async {
    await _prefs.setString(AppConstants.tokenKey, token);
  }

  Future<void> saveRefreshToken(String refreshToken) async {
    await _prefs.setString(AppConstants.refreshTokenKey, refreshToken);
  }

  Future<void> saveUserEmail(String email) async {
    await _prefs.setString(AppConstants.userEmailKey, email);
  }

  String? getToken() {
    return _prefs.getString(AppConstants.tokenKey);
  }

  String? getRefreshToken() {
    return _prefs.getString(AppConstants.refreshTokenKey);
  }

  String? getUserEmail() {
    return _prefs.getString(AppConstants.userEmailKey);
  }

  Future<void> clearAuth() async {
    await _prefs.remove(AppConstants.tokenKey);
    await _prefs.remove(AppConstants.refreshTokenKey);
    await _prefs.remove(AppConstants.userEmailKey);
  }

  bool get isAuthenticated => getToken() != null;
}
