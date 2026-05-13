import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../providers/patient_provider.dart';
import '../../providers/auth_provider.dart';
import '../../../data/services/settings_service.dart';
import '../../../core/utils/date_formatter.dart';
import '../../../core/utils/validators.dart';
import '../../../core/utils/input_formatters.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/constants/app_constants.dart';
import '../auth/forgot_password_screen.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final _formKey = GlobalKey<FormState>();
  final _firstNameController = TextEditingController();
  final _lastNameController = TextEditingController();
  final _middleNameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _allergiesController = TextEditingController();
  final _chronicDiseasesController = TextEditingController();

  bool _notifyAppointmentReminder = true;
  bool _notifyRatingReminder = true;
  bool _notifyAppointmentCancelled = true;

  String _defaultScreen = 'search';

  bool _isLoadingSettings = true;

  String? _getAvatarUrl(String? avatarPath) {
    if (avatarPath == null || avatarPath.isEmpty) return null;
    if (avatarPath.startsWith('http')) return avatarPath;

    final baseUrl = ApiConstants.baseUrl.replaceAll('/api', '');
    return '$baseUrl$avatarPath';
  }

  Future<Map<String, String>> _getHeaders() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString(AppConstants.tokenKey);
    return {
      'Authorization': 'Bearer ${token ?? ''}',
    };
  }

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  String _formatPhoneForDisplay(String phone) {
    // Убираем все нецифровые символы
    final digits = phone.replaceAll(RegExp(r'[^0-9]'), '');

    if (digits.isEmpty) return '+7 ';

    // Всегда начинаем с 7
    String cleanDigits = digits;
    if (cleanDigits.startsWith('8')) {
      cleanDigits = '7${cleanDigits.substring(1)}';
    } else if (!cleanDigits.startsWith('7')) {
      cleanDigits = '7$cleanDigits';
    }

    // Ограничиваем 11 цифрами
    if (cleanDigits.length > 11) {
      cleanDigits = cleanDigits.substring(0, 11);
    }

    // Форматируем: +7 (999) 123-45-67
    final buffer = StringBuffer('+7');

    if (cleanDigits.length > 1) {
      buffer.write(' (');
      buffer.write(cleanDigits.substring(1, cleanDigits.length > 4 ? 4 : cleanDigits.length));

      if (cleanDigits.length >= 4) {
        buffer.write(') ');
        buffer.write(cleanDigits.substring(4, cleanDigits.length > 7 ? 7 : cleanDigits.length));

        if (cleanDigits.length >= 7) {
          buffer.write('-');
          buffer.write(cleanDigits.substring(7, cleanDigits.length > 9 ? 9 : cleanDigits.length));

          if (cleanDigits.length >= 9) {
            buffer.write('-');
            buffer.write(cleanDigits.substring(9));
          }
        }
      }
    }

    return buffer.toString();
  }

  Future<void> _loadData() async {
    final patient = context.read<PatientProvider>().currentPatient;
    if (patient != null) {
      _firstNameController.text = patient.firstName;
      _lastNameController.text = patient.lastName;
      _middleNameController.text = patient.middleName ?? '';
      _phoneController.text = _formatPhoneForDisplay(patient.phoneNumber);
      _allergiesController.text = patient.allergies ?? '';
      _chronicDiseasesController.text = patient.chronicDiseases ?? '';
    }

    // Load settings from backend
    final patientProvider = context.read<PatientProvider>();
    final settingsService = context.read<SettingsService>();
    final settings = await patientProvider.getMySettings();

    if (settings != null && mounted) {
      // Update local state
      setState(() {
        _notifyAppointmentReminder = settings['notifyAppointmentReminder'] ?? true;
        _notifyRatingReminder = settings['notifyRatingReminder'] ?? true;
        _notifyAppointmentCancelled = settings['notifyAppointmentCancelled'] ?? true;
        _defaultScreen = settings['defaultScreen'] ?? 'search';
        _isLoadingSettings = false;
      });

      // Sync with local storage
      await settingsService.setDefaultScreen(_defaultScreen);
      await settingsService.setNotifyAppointmentReminder(_notifyAppointmentReminder);
      await settingsService.setNotifyAppointmentCancelled(_notifyAppointmentCancelled);
    } else if (mounted) {
      setState(() {
        _isLoadingSettings = false;
      });
    }
  }

  @override
  void dispose() {
    _firstNameController.dispose();
    _lastNameController.dispose();
    _middleNameController.dispose();
    _phoneController.dispose();
    _allergiesController.dispose();
    _chronicDiseasesController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    final ImagePicker picker = ImagePicker();

    final source = await showDialog<ImageSource>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Выберите источник'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.camera_alt),
              title: const Text('Камера'),
              onTap: () => Navigator.pop(context, ImageSource.camera),
            ),
            ListTile(
              leading: const Icon(Icons.photo_library),
              title: const Text('Галерея'),
              onTap: () => Navigator.pop(context, ImageSource.gallery),
            ),
          ],
        ),
      ),
    );

    if (source == null) return;

    final XFile? image = await picker.pickImage(source: source);

    if (image != null && mounted) {
      // Crop the image
      final croppedFile = await ImageCropper().cropImage(
        sourcePath: image.path,
        maxWidth: 512,
        maxHeight: 512,
        compressQuality: 95,
        compressFormat: ImageCompressFormat.jpg,
        aspectRatio: const CropAspectRatio(ratioX: 1, ratioY: 1),
        uiSettings: [
          AndroidUiSettings(
            toolbarTitle: 'Обрезать изображение',
            toolbarColor: Theme.of(context).colorScheme.primary,
            toolbarWidgetColor: Colors.white,
            lockAspectRatio: true,
            hideBottomControls: false,
            initAspectRatio: CropAspectRatioPreset.square,
          ),
          IOSUiSettings(
            title: 'Обрезать изображение',
            aspectRatioLockEnabled: true,
          ),
        ],
      );

      if (croppedFile != null && mounted) {
        final patientProvider = context.read<PatientProvider>();
        final success = await patientProvider.uploadAvatar(croppedFile.path);

        if (mounted) {
          if (success) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Фото профиля обновлено')),
            );
          } else {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Ошибка: ${patientProvider.error}')),
            );
          }
        }
      }
    }
  }

  Future<void> _deleteAvatar() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Удалить фото'),
        content: const Text('Вы уверены, что хотите удалить фото профиля?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Отмена'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Удалить'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      final patientProvider = context.read<PatientProvider>();
      final success = await patientProvider.deleteAvatar();

      if (mounted) {
        if (success) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Фото профиля удалено')),
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Ошибка: ${patientProvider.error}')),
          );
        }
      }
    }
  }

  Future<void> _savePersonalData() async {
    if (_formKey.currentState!.validate()) {
      final patientProvider = context.read<PatientProvider>();

      // Clean phone number - remove all non-digits
      String cleanedPhone = InputFormatUtils.cleanPhone(_phoneController.text);

      final success = await patientProvider.updateMyProfile(
        firstName: _firstNameController.text.trim(),
        lastName: _lastNameController.text.trim(),
        middleName: _middleNameController.text.trim().isEmpty ? null : _middleNameController.text.trim(),
        phoneNumber: cleanedPhone,
        allergies: _allergiesController.text.trim().isEmpty ? null : _allergiesController.text.trim(),
        chronicDiseases: _chronicDiseasesController.text.trim().isEmpty ? null : _chronicDiseasesController.text.trim(),
      );

      if (mounted) {
        if (success) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Личные данные сохранены')),
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Ошибка: ${patientProvider.error}')),
          );
        }
      }
    }
  }

  Future<void> _saveAppSettings() async {
    final patientProvider = context.read<PatientProvider>();
    final settingsService = context.read<SettingsService>();

    final success = await patientProvider.updateMySettings(
      notifyAppointmentReminder: _notifyAppointmentReminder,
      notifyRatingReminder: _notifyRatingReminder,
      notifyAppointmentCancelled: _notifyAppointmentCancelled,
      defaultScreen: _defaultScreen,
    );

    if (success) {
      // Save to local storage as well
      await settingsService.setDefaultScreen(_defaultScreen);
      await settingsService.setNotifyAppointmentReminder(_notifyAppointmentReminder);
      await settingsService.setNotifyAppointmentCancelled(_notifyAppointmentCancelled);
    }

    if (mounted) {
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Настройки приложения сохранены')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Ошибка: ${patientProvider.error}')),
        );
      }
    }
  }

  void _changePassword() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const ForgotPasswordScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    final patient = context.watch<PatientProvider>().currentPatient;

    if (patient == null) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Настройки'),
        ),
        body: const Center(child: Text('Данные не загружены')),
      );
    }

    if (_isLoadingSettings) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Настройки'),
        ),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Настройки'),
      ),
      body: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Personal Data Section
                  Text(
                    'Личные данные',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Avatar
                  Center(
                    child: Stack(
                      children: [
                        FutureBuilder<Map<String, String>>(
                          future: _getHeaders(),
                          builder: (context, snapshot) {
                            final avatarUrl = _getAvatarUrl(patient.avatarUrl);

                            if (avatarUrl != null && snapshot.hasData) {
                              return CircleAvatar(
                                radius: 50,
                                backgroundImage: CachedNetworkImageProvider(
                                  avatarUrl,
                                  headers: snapshot.data,
                                ),
                              );
                            }

                            return const CircleAvatar(
                              radius: 50,
                              child: Icon(Icons.person, size: 50),
                            );
                          },
                        ),
                        Positioned(
                          bottom: 0,
                          right: 0,
                          child: CircleAvatar(
                            radius: 18,
                            backgroundColor: Theme.of(context).colorScheme.primary,
                            child: PopupMenuButton<String>(
                              icon: const Icon(Icons.camera_alt, size: 18, color: Colors.white),
                              onSelected: (value) {
                                if (value == 'upload') {
                                  _pickImage();
                                } else if (value == 'delete') {
                                  _deleteAvatar();
                                }
                              },
                              itemBuilder: (context) => [
                                const PopupMenuItem(
                                  value: 'upload',
                                  child: Row(
                                    children: [
                                      Icon(Icons.upload),
                                      SizedBox(width: 8),
                                      Text('Загрузить фото'),
                                    ],
                                  ),
                                ),
                                if (patient.avatarUrl != null && patient.avatarUrl!.isNotEmpty)
                                  const PopupMenuItem(
                                    value: 'delete',
                                    child: Row(
                                      children: [
                                        Icon(Icons.delete),
                                        SizedBox(width: 8),
                                        Text('Удалить фото'),
                                      ],
                                    ),
                                  ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Last Name
                  TextFormField(
                    controller: _lastNameController,
                    decoration: const InputDecoration(
                      labelText: 'Фамилия',
                      prefixIcon: Icon(Icons.person),
                    ),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return 'Введите фамилию';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // First Name
                  TextFormField(
                    controller: _firstNameController,
                    decoration: const InputDecoration(
                      labelText: 'Имя',
                      prefixIcon: Icon(Icons.person),
                    ),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return 'Введите имя';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Middle Name
                  TextFormField(
                    controller: _middleNameController,
                    decoration: const InputDecoration(
                      labelText: 'Отчество',
                      prefixIcon: Icon(Icons.person),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Date of Birth (read-only)
                  ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const Icon(Icons.calendar_today),
                    title: const Text('Дата рождения'),
                    subtitle: Text(DateFormatter.formatDate(patient.dateOfBirth)),
                    trailing: const Icon(Icons.lock_outline),
                  ),
                  const SizedBox(height: 8),

                  // SNILS (read-only)
                  ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const Icon(Icons.badge),
                    title: const Text('СНИЛС'),
                    subtitle: Text(patient.snils),
                    trailing: const Icon(Icons.lock_outline),
                  ),
                  const SizedBox(height: 16),

                  // Phone
                  TextFormField(
                    controller: _phoneController,
                    decoration: const InputDecoration(
                      labelText: 'Телефон',
                      prefixIcon: Icon(Icons.phone),
                      hintText: '+7 (999) 123-45-67',
                    ),
                    keyboardType: TextInputType.phone,
                    inputFormatters: [PhoneInputFormatter()],
                    validator: Validators.validatePhone,
                  ),
                  const SizedBox(height: 16),

                  // Allergies
                  TextFormField(
                    controller: _allergiesController,
                    decoration: const InputDecoration(
                      labelText: 'Аллергии',
                      prefixIcon: Icon(Icons.warning),
                    ),
                    maxLines: 2,
                  ),
                  const SizedBox(height: 16),

                  // Chronic Diseases
                  TextFormField(
                    controller: _chronicDiseasesController,
                    decoration: const InputDecoration(
                      labelText: 'Хронические заболевания',
                      prefixIcon: Icon(Icons.medical_information),
                    ),
                    maxLines: 2,
                  ),
                  const SizedBox(height: 24),

                  // Save Personal Data Button
                  ElevatedButton(
                    onPressed: _savePersonalData,
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: const Text('Сохранить данные'),
                  ),

                  const SizedBox(height: 32),
                  const Divider(),
                  const SizedBox(height: 16),

                  // Security Section
                  Text(
                    'Безопасность',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Email (read-only)
                  ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const Icon(Icons.email),
                    title: const Text('Email'),
                    subtitle: Text(patient.email),
                    trailing: const Icon(Icons.lock_outline),
                  ),
                  const SizedBox(height: 8),

                  // Change Password Button
                  ElevatedButton(
                    onPressed: _changePassword,
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: const Text('Сменить пароль'),
                  ),

                  const SizedBox(height: 32),
                  const Divider(),
                  const SizedBox(height: 16),

                  // App Settings Section
                  Text(
                    'Настройки приложения',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Default Screen
                  DropdownButtonFormField<String>(
                    initialValue: _defaultScreen,
                    decoration: const InputDecoration(
                      labelText: 'Экран по умолчанию',
                      prefixIcon: Icon(Icons.home),
                    ),
                    items: const [
                      DropdownMenuItem(value: 'search', child: Text('Поиск')),
                      DropdownMenuItem(value: 'appointments', child: Text('Мои записи')),
                      DropdownMenuItem(value: 'medical_card', child: Text('Медкарта')),
                      DropdownMenuItem(value: 'profile', child: Text('Профиль')),
                    ],
                    onChanged: (value) {
                      setState(() {
                        _defaultScreen = value!;
                      });
                    },
                  ),

                  const SizedBox(height: 24),

                  // Notification Settings
                  Text(
                    'Настройки уведомлений',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),

                  SwitchListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('Напоминания о приеме'),
                    subtitle: const Text('За 2 часа до приема'),
                    value: _notifyAppointmentReminder,
                    onChanged: (value) {
                      setState(() {
                        _notifyAppointmentReminder = value;
                      });
                    },
                  ),

                  SwitchListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('Напоминание об оценке врача'),
                    subtitle: const Text('После завершения приема'),
                    value: _notifyRatingReminder,
                    onChanged: (value) {
                      setState(() {
                        _notifyRatingReminder = value;
                      });
                    },
                  ),

                  SwitchListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('Уведомления об отмене приема'),
                    subtitle: const Text('Когда врач отменяет прием'),
                    value: _notifyAppointmentCancelled,
                    onChanged: (value) {
                      setState(() {
                        _notifyAppointmentCancelled = value;
                      });
                    },
                  ),

                  const SizedBox(height: 24),

                  // Save App Settings Button
                  ElevatedButton(
                    onPressed: _saveAppSettings,
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 48),
                    ),
                    child: const Text('Сохранить настройки приложения'),
                  ),

                  const SizedBox(height: 32),
                ],
              ),
            ),
          ),
    );
  }
}
