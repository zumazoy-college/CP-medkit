import 'package:flutter/services.dart';

/// Форматтер для СНИЛС: 123-456-789 01
class SnilsInputFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final text = newValue.text.replaceAll(RegExp(r'[^0-9]'), '');

    if (text.length > 11) {
      return oldValue;
    }

    if (text.isEmpty) {
      return const TextEditingValue();
    }

    final buffer = StringBuffer();
    for (int i = 0; i < text.length; i++) {
      buffer.write(text[i]);
      if (i == 2 || i == 5 || i == 8) {
        buffer.write(i == 8 ? ' ' : '-');
      }
    }

    final formatted = buffer.toString();

    // Сохраняем позицию курсора
    int cursorPosition = formatted.length;

    return TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(offset: cursorPosition),
    );
  }
}

/// Форматтер для телефона: +7 (999) 123-45-67
class PhoneInputFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final text = newValue.text.replaceAll(RegExp(r'[^0-9]'), '');

    if (text.isEmpty) {
      return const TextEditingValue(
        text: '+7 ',
        selection: TextSelection.collapsed(offset: 3),
      );
    }

    // Всегда начинаем с 7, даже если пользователь ввел 8
    String digits = text;
    if (digits.startsWith('8')) {
      digits = '7' + digits.substring(1);
    } else if (!digits.startsWith('7')) {
      digits = '7' + digits;
    }

    // Ограничиваем 11 цифрами (7 + 10 цифр номера)
    if (digits.length > 11) {
      digits = digits.substring(0, 11);
    }

    final buffer = StringBuffer();
    buffer.write('+7');

    if (digits.length > 1) {
      buffer.write(' (');
      buffer.write(digits.substring(1, digits.length > 4 ? 4 : digits.length));

      if (digits.length >= 4) {
        buffer.write(') ');
        buffer.write(digits.substring(4, digits.length > 7 ? 7 : digits.length));

        if (digits.length >= 7) {
          buffer.write('-');
          buffer.write(digits.substring(7, digits.length > 9 ? 9 : digits.length));

          if (digits.length >= 9) {
            buffer.write('-');
            buffer.write(digits.substring(9, digits.length));
          }
        }
      }
    }

    final formatted = buffer.toString();

    // Сохраняем позицию курсора
    int cursorPosition = formatted.length;

    return TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(offset: cursorPosition),
    );
  }
}

/// Утилита для получения чистых данных (без форматирования)
class InputFormatUtils {
  /// Убирает форматирование из СНИЛС: "123-456-789 01" -> "12345678901"
  static String cleanSnils(String formatted) {
    return formatted.replaceAll(RegExp(r'[^0-9]'), '');
  }

  /// Убирает форматирование из телефона: "+7 (999) 123-45-67" -> "79991234567"
  static String cleanPhone(String formatted) {
    return formatted.replaceAll(RegExp(r'[^0-9]'), '');
  }
}
