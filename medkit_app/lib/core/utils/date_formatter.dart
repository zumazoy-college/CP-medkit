import 'package:intl/intl.dart';

class DateFormatter {
  static String formatDate(DateTime date) {
    return DateFormat('dd.MM.yyyy').format(date);
  }

  static String formatTime(DateTime time) {
    return DateFormat('HH:mm').format(time);
  }

  static String formatDateTime(DateTime dateTime) {
    return DateFormat('dd.MM.yyyy HH:mm').format(dateTime);
  }

  static String formatDateForApi(DateTime date) {
    return DateFormat('yyyy-MM-dd').format(date);
  }

  static String formatTimeForApi(DateTime time) {
    return DateFormat('HH:mm:ss').format(time);
  }

  static DateTime? parseDate(String? dateString) {
    if (dateString == null || dateString.isEmpty) return null;
    try {
      return DateTime.parse(dateString);
    } catch (e) {
      return null;
    }
  }

  static String getRelativeDate(DateTime date) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final targetDate = DateTime(date.year, date.month, date.day);

    final difference = targetDate.difference(today).inDays;

    if (difference == 0) {
      return 'Сегодня';
    } else if (difference == 1) {
      return 'Завтра';
    } else if (difference == -1) {
      return 'Вчера';
    } else {
      return formatDate(date);
    }
  }
}
