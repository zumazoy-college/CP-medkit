import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../../core/utils/validators.dart';
import '../../core/utils/input_formatters.dart';
import 'home/home_screen.dart';

class RegisterScreen extends StatefulWidget {
  final String? email;
  final bool isEmailVerified;

  const RegisterScreen({
    super.key,
    this.email,
    this.isEmailVerified = false,
  });

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _lastNameController = TextEditingController();
  final _firstNameController = TextEditingController();
  final _middleNameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _snilsController = TextEditingController();
  DateTime? _dateOfBirth;
  String _gender = 'MALE';
  bool _obscurePassword = true;

  @override
  void initState() {
    super.initState();
    if (widget.email != null) {
      _emailController.text = widget.email!;
    }
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _lastNameController.dispose();
    _firstNameController.dispose();
    _middleNameController.dispose();
    _phoneController.dispose();
    _snilsController.dispose();
    super.dispose();
  }

  Future<void> _selectDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: DateTime(2000),
      firstDate: DateTime(1900),
      lastDate: DateTime.now(),
    );
    if (picked != null) {
      setState(() {
        _dateOfBirth = picked;
      });
    }
  }

  Future<void> _register() async {
    if (_formKey.currentState!.validate()) {
      if (_dateOfBirth == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Выберите дату рождения')),
        );
        return;
      }

      final authProvider = context.read<AuthProvider>();
      final success = await authProvider.register(
        email: _emailController.text.trim(),
        password: _passwordController.text,
        lastName: _lastNameController.text.trim(),
        firstName: _firstNameController.text.trim(),
        middleName: _middleNameController.text.trim().isEmpty
            ? null
            : _middleNameController.text.trim(),
        phoneNumber: InputFormatUtils.cleanPhone(_phoneController.text),
        dateOfBirth: _dateOfBirth!,
        gender: _gender,
        snils: InputFormatUtils.cleanSnils(_snilsController.text),
      );

      if (success && mounted) {
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const HomeScreen()),
          (route) => false,
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Регистрация'),
        automaticallyImplyLeading: false,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextFormField(
                  controller: _lastNameController,
                  maxLength: 50,
                  decoration: const InputDecoration(
                    labelText: 'Фамилия',
                    prefixIcon: Icon(Icons.person),
                    counterText: '',
                  ),
                  validator: (value) => Validators.validateRequired(value, 'фамилию'),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _firstNameController,
                  maxLength: 50,
                  decoration: const InputDecoration(
                    labelText: 'Имя',
                    prefixIcon: Icon(Icons.person),
                    counterText: '',
                  ),
                  validator: (value) => Validators.validateRequired(value, 'имя'),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _middleNameController,
                  maxLength: 50,
                  decoration: const InputDecoration(
                    labelText: 'Отчество (необязательно)',
                    prefixIcon: Icon(Icons.person),
                    counterText: '',
                  ),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  enabled: !widget.isEmailVerified,
                  decoration: InputDecoration(
                    labelText: 'Email',
                    prefixIcon: const Icon(Icons.email),
                    suffixIcon: widget.isEmailVerified
                        ? const Icon(Icons.check_circle, color: Colors.green)
                        : null,
                  ),
                  validator: Validators.validateEmail,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _passwordController,
                  obscureText: _obscurePassword,
                  decoration: InputDecoration(
                    labelText: 'Пароль',
                    prefixIcon: const Icon(Icons.lock),
                    suffixIcon: IconButton(
                      icon: Icon(
                        _obscurePassword
                            ? Icons.visibility
                            : Icons.visibility_off,
                      ),
                      onPressed: () {
                        setState(() {
                          _obscurePassword = !_obscurePassword;
                        });
                      },
                    ),
                  ),
                  validator: Validators.validatePassword,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _phoneController,
                  keyboardType: TextInputType.phone,
                  inputFormatters: [PhoneInputFormatter()],
                  decoration: const InputDecoration(
                    labelText: 'Телефон',
                    prefixIcon: Icon(Icons.phone),
                    hintText: '+7 (999) 123-45-67',
                  ),
                  validator: Validators.validatePhone,
                ),
                const SizedBox(height: 16),
                InkWell(
                  onTap: _selectDate,
                  child: InputDecorator(
                    decoration: const InputDecoration(
                      labelText: 'Дата рождения',
                      prefixIcon: Icon(Icons.calendar_today),
                    ),
                    child: Text(
                      _dateOfBirth == null
                          ? 'Выберите дату'
                          : '${_dateOfBirth!.day}.${_dateOfBirth!.month}.${_dateOfBirth!.year}',
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  initialValue: _gender,
                  decoration: const InputDecoration(
                    labelText: 'Пол',
                    prefixIcon: Icon(Icons.person_outline),
                  ),
                  items: const [
                    DropdownMenuItem(value: 'MALE', child: Text('Мужской')),
                    DropdownMenuItem(value: 'FEMALE', child: Text('Женский')),
                  ],
                  onChanged: (value) {
                    if (value != null) {
                      setState(() {
                        _gender = value;
                      });
                    }
                  },
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _snilsController,
                  keyboardType: TextInputType.number,
                  inputFormatters: [SnilsInputFormatter()],
                  decoration: const InputDecoration(
                    labelText: 'СНИЛС',
                    prefixIcon: Icon(Icons.badge),
                    hintText: '123-456-789 01',
                  ),
                  validator: Validators.validateSnils,
                ),
                const SizedBox(height: 32),
                Consumer<AuthProvider>(
                  builder: (context, authProvider, child) {
                    if (authProvider.error != null) {
                      WidgetsBinding.instance.addPostFrameCallback((_) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(authProvider.error!),
                            backgroundColor: Colors.red,
                          ),
                        );
                        authProvider.clearError();
                      });
                    }

                    return ElevatedButton(
                      onPressed: authProvider.isLoading ? null : _register,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: authProvider.isLoading
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('Зарегистрироваться'),
                    );
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
