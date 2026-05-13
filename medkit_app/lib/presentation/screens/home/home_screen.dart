import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/favorite_provider.dart';
import '../../../data/services/settings_service.dart';
import '../search/search_screen.dart';
import '../appointments/my_appointments_screen.dart';
import '../medical_card/medical_card_screen.dart';
import '../profile/profile_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late int _currentIndex;

  void _navigateToTab(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  void initState() {
    super.initState();

    // Load default screen from settings
    final settingsService = context.read<SettingsService>();
    final defaultScreen = settingsService.getDefaultScreen();
    _currentIndex = _getIndexFromScreen(defaultScreen);

    // Load favorites when home screen initializes
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<FavoriteProvider>().loadFavorites();
    });
  }

  int _getIndexFromScreen(String screen) {
    switch (screen) {
      case 'search':
        return 0;
      case 'appointments':
        return 1;
      case 'medical_card':
        return 2;
      case 'profile':
        return 3;
      default:
        return 0;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: [
          const SearchScreen(),
          const MyAppointmentsScreen(),
          const MedicalCardScreen(),
          ProfileScreen(onNavigateToTab: _navigateToTab),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.search),
            label: 'Поиск',
          ),
          NavigationDestination(
            icon: Icon(Icons.calendar_today),
            label: 'Мои записи',
          ),
          NavigationDestination(
            icon: Icon(Icons.medical_information),
            label: 'Медкарта',
          ),
          NavigationDestination(
            icon: Icon(Icons.person),
            label: 'Профиль',
          ),
        ],
      ),
    );
  }
}
