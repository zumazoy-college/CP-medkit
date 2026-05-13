import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import '../../providers/doctor_provider.dart';
import '../../providers/favorite_provider.dart';
import '../doctor/doctor_detail_screen.dart';
import '../../../core/constants/api_constants.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final _searchController = TextEditingController();
  double? _minRating;
  int? _minExperience;
  String? _selectedGender;
  String _sortBy = 'rating';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DoctorProvider>().loadDoctors();
      context.read<FavoriteProvider>().loadFavorites();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _applyFilters() {
    List<String>? sortParams;
    switch (_sortBy) {
      case 'experience':
        sortParams = ['experienceYears,desc'];
        break;
      case 'nearestSlot':
        sortParams = ['nextAvailableSlotDate,asc', 'nextAvailableSlotTime,asc'];
        break;
      case 'rating':
      default:
        sortParams = ['rating,desc'];
        break;
    }

    // Search text - send only to name parameter which searches in full name
    final searchText = _searchController.text.trim();

    context.read<DoctorProvider>().searchDoctors(
          specialization: null,
          minRating: _minRating,
          name: searchText.isNotEmpty ? searchText : null,
          gender: _selectedGender,
          minExperience: _minExperience,
          sort: sortParams,
        );
  }

  void _showFilters() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (context) => StatefulBuilder(
        builder: (context, setModalState) => Padding(
          padding: EdgeInsets.only(
            left: 16.0,
            right: 16.0,
            top: 16.0,
            bottom: MediaQuery.of(context).viewInsets.bottom + 16.0,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  'Фильтры',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<double>(
                  initialValue: _minRating,
                  decoration: const InputDecoration(
                    labelText: 'Минимальный рейтинг',
                  ),
                  items: const [
                    DropdownMenuItem(value: null, child: Text('Любой')),
                    DropdownMenuItem(value: 4.0, child: Text('4.0 и выше')),
                    DropdownMenuItem(value: 4.5, child: Text('4.5 и выше')),
                  ],
                  onChanged: (value) {
                    setModalState(() {
                      _minRating = value;
                    });
                  },
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  initialValue: _selectedGender,
                  decoration: const InputDecoration(
                    labelText: 'Пол врача',
                  ),
                  items: const [
                    DropdownMenuItem(value: null, child: Text('Любой')),
                    DropdownMenuItem(value: 'male', child: Text('Мужской')),
                    DropdownMenuItem(value: 'female', child: Text('Женский')),
                  ],
                  onChanged: (value) {
                    setModalState(() {
                      _selectedGender = value;
                    });
                  },
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<int>(
                  initialValue: _minExperience,
                  decoration: const InputDecoration(
                    labelText: 'Минимальный стаж (лет)',
                  ),
                  items: const [
                    DropdownMenuItem(value: null, child: Text('Любой')),
                    DropdownMenuItem(value: 5, child: Text('5 лет и более')),
                    DropdownMenuItem(value: 10, child: Text('10 лет и более')),
                    DropdownMenuItem(value: 15, child: Text('15 лет и более')),
                  ],
                  onChanged: (value) {
                    setModalState(() {
                      _minExperience = value;
                    });
                  },
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  initialValue: _sortBy,
                  decoration: const InputDecoration(
                    labelText: 'Сортировка',
                  ),
                  items: const [
                    DropdownMenuItem(value: 'rating', child: Text('По рейтингу')),
                    DropdownMenuItem(value: 'experience', child: Text('По стажу')),
                    DropdownMenuItem(value: 'nearestSlot', child: Text('По ближайшей записи')),
                  ],
                  onChanged: (value) {
                    setModalState(() {
                      _sortBy = value!;
                    });
                  },
                ),
                const SizedBox(height: 24),
                ElevatedButton(
                  onPressed: () {
                    Navigator.pop(context);
                    _applyFilters();
                  },
                  child: const Text('Применить'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Поиск врачей'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: _showFilters,
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Поиск по ФИО или специализации',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          _applyFilters();
                        },
                      )
                    : null,
              ),
              onChanged: (value) {
                setState(() {});
                // Автоматический поиск с небольшой задержкой
                Future.delayed(const Duration(milliseconds: 500), () {
                  if (_searchController.text == value) {
                    _applyFilters();
                  }
                });
              },
            ),
          ),
          Expanded(
            child: Consumer<DoctorProvider>(
              builder: (context, doctorProvider, child) {
                if (doctorProvider.isLoading) {
                  return const Center(child: CircularProgressIndicator());
                }

                if (doctorProvider.error != null) {
                  return Center(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            doctorProvider.error!,
                            textAlign: TextAlign.center,
                            maxLines: 5,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 16),
                          ElevatedButton(
                            onPressed: () => doctorProvider.loadDoctors(),
                            child: const Text('Повторить'),
                          ),
                        ],
                      ),
                    ),
                  );
                }

                final doctors = doctorProvider.doctors;

                if (doctors.isEmpty) {
                  return const Center(
                    child: Text('Врачи не найдены'),
                  );
                }

                return RefreshIndicator(
                  onRefresh: () => doctorProvider.loadDoctors(),
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: doctors.length,
                    itemBuilder: (context, index) {
                      final doctor = doctors[index];
                      return _DoctorCard(doctor: doctor);
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _DoctorCard extends StatelessWidget {
  final dynamic doctor;

  const _DoctorCard({required this.doctor});

  String? _getAvatarUrl(String? avatarPath) {
    if (avatarPath == null || avatarPath.isEmpty) return null;
    if (avatarPath.startsWith('http')) return avatarPath;

    // Get base URL without /api suffix
    final baseUrl = ApiConstants.baseUrl.replaceAll('/api', '');
    return '$baseUrl$avatarPath';
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => DoctorDetailScreen(doctorId: doctor.idDoctor),
            ),
          );
        },
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            children: [
              CircleAvatar(
                radius: 30,
                backgroundImage: _getAvatarUrl(doctor.avatarUrl) != null
                    ? NetworkImage(_getAvatarUrl(doctor.avatarUrl)!)
                    : null,
                child: _getAvatarUrl(doctor.avatarUrl) == null
                    ? const Icon(Icons.person, size: 30)
                    : null,
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      doctor.fullName,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      doctor.specialization,
                      style: Theme.of(context).textTheme.bodyMedium,
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        RatingBarIndicator(
                          rating: doctor.rating,
                          itemBuilder: (context, _) => const Icon(
                            Icons.star,
                            color: Colors.amber,
                          ),
                          itemCount: 5,
                          itemSize: 16,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          '${doctor.rating.toStringAsFixed(1)} (${doctor.reviewsCount})',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                    if (doctor.nextAvailableSlot != null) ...[
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          const Icon(Icons.calendar_today, size: 14, color: Colors.green),
                          const SizedBox(width: 4),
                          Text(
                            doctor.nextAvailableSlot!,
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                  color: Colors.green,
                                ),
                          ),
                        ],
                      ),
                    ],
                  ],
                ),
              ),
              Consumer<FavoriteProvider>(
                builder: (context, favoriteProvider, child) {
                  final isFavorite = favoriteProvider.isFavorite(doctor.idDoctor);
                  return IconButton(
                    icon: Icon(
                      isFavorite ? Icons.favorite : Icons.favorite_border,
                      color: isFavorite ? Colors.red : null,
                    ),
                    onPressed: () {
                      favoriteProvider.toggleFavorite(doctor.idDoctor);
                    },
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
