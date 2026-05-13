import '../constants/app_constants.dart';

class Validators {
  static String? validateEmail(String? value) {
    if (value == null || value.isEmpty) {
      return 'Введите email';
    }
    final emailRegex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    if (!emailRegex.hasMatch(value)) {
      return 'Введите корректный email';
    }
    return null;
  }

  static String? validatePassword(String? value) {
    if (value == null || value.isEmpty) {
      return 'Введите пароль';
    }
    if (value.length < AppConstants.minPasswordLength) {
      return 'Пароль должен содержать минимум ${AppConstants.minPasswordLength} символов';
    }
    if (!RegExp(r'[a-z]').hasMatch(value)) {
      return 'Пароль должен содержать хотя бы одну строчную букву';
    }
    if (!RegExp(r'[A-Z]').hasMatch(value)) {
      return 'Пароль должен содержать хотя бы одну заглавную букву';
    }
    if (!RegExp(r'[0-9]').hasMatch(value)) {
      return 'Пароль должен содержать хотя бы одну цифру';
    }
    return null;
  }

  static String? validateRequired(String? value, String fieldName) {
    if (value == null || value.isEmpty) {
      return 'Введите $fieldName';
    }
    return null;
  }

  static String? validatePhone(String? value) {
    if (value == null || value.isEmpty) {
      return 'Введите номер телефона';
    }
    final phoneRegex = RegExp(r'^\+?[0-9]{10,15}$');
    if (!phoneRegex.hasMatch(value.replaceAll(RegExp(r'[\s\-\(\)]'), ''))) {
      return 'Введите корректный номер телефона';
    }
    return null;
  }

  static String? validateSnils(String? value) {
    if (value == null || value.isEmpty) {
      return 'Введите СНИЛС';
    }
    // Убираем форматирование для проверки
    final cleanSnils = value.replaceAll(RegExp(r'[^0-9]'), '');
    if (cleanSnils.length != 11) {
      return 'СНИЛС должен содержать 11 цифр';
    }
    return null;
  }
}
