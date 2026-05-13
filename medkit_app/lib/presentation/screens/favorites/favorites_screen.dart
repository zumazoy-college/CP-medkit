import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import '../../providers/favorite_provider.dart';
import '../../../data/models/favorite_model.dart';
import '../doctor/doctor_detail_screen.dart';
import '../../../core/constants/api_constants.dart';

class FavoritesScreen extends StatefulWidget {
  const FavoritesScreen({super.key});

  @override
  State<FavoritesScreen> createState() => _FavoritesScreenState();
}

class _FavoritesScreenState extends State<FavoritesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<FavoriteProvider>().loadFavorites();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Избранные врачи'),
      ),
      body: Consumer<FavoriteProvider>(
        builder: (context, favoriteProvider, child) {
          if (favoriteProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (favoriteProvider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(favoriteProvider.error!),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => favoriteProvider.loadFavorites(),
                    child: const Text('Повторить'),
                  ),
                ],
              ),
            );
          }

          if (favoriteProvider.favorites.isEmpty) {
            return const Center(
              child: Text('Нет избранных врачей'),
            );
          }

          return RefreshIndicator(
            onRefresh: () => favoriteProvider.loadFavorites(),
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: favoriteProvider.favorites.length,
              itemBuilder: (context, index) {
                final favorite = favoriteProvider.favorites[index];
                return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: InkWell(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) =>
                            DoctorDetailScreen(doctorId: favorite.doctorId),
                      ),
                    );
                  },
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Row(
                      children: [
                        CircleAvatar(
                          radius: 30,
                          backgroundImage: _getAvatarUrl(favorite.avatarUrl) != null
                              ? NetworkImage(_getAvatarUrl(favorite.avatarUrl)!)
                              : null,
                          child: _getAvatarUrl(favorite.avatarUrl) == null
                              ? const Icon(Icons.person, size: 30)
                              : null,
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                favorite.doctorName,
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 4),
                              Text(
                                favorite.specialization,
                                style: Theme.of(context).textTheme.bodyMedium,
                              ),
                              const SizedBox(height: 8),
                              Row(
                                children: [
                                  RatingBarIndicator(
                                    rating: favorite.rating,
                                    itemBuilder: (context, _) => const Icon(
                                      Icons.star,
                                      color: Colors.amber,
                                    ),
                                    itemCount: 5,
                                    itemSize: 16,
                                  ),
                                  const SizedBox(width: 8),
                                  Text(
                                    favorite.rating.toStringAsFixed(1),
                                    style:
                                        Theme.of(context).textTheme.bodySmall,
                                  ),
                                ],
                              ),
                              const SizedBox(height: 4),
                              _buildSlotAvailability(context, favorite),
                            ],
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.favorite, color: Colors.red),
                          onPressed: () {
                            favoriteProvider.toggleFavorite(favorite.doctorId);
                          },
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        );
      },
    ),
  );
}

  Widget _buildSlotAvailability(BuildContext context, FavoriteModel favorite) {
    if (favorite.nextAvailableSlot == null) {
      return Row(
        children: [
          Icon(Icons.schedule, size: 14, color: Colors.grey[600]),
          const SizedBox(width: 4),
          Text(
            'Нет доступных слотов',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: Colors.grey[600],
            ),
          ),
        ],
      );
    }

    return Row(
      children: [
        const Icon(Icons.calendar_today, size: 14, color: Colors.green),
        const SizedBox(width: 4),
        Text(
          favorite.nextAvailableSlot!,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Colors.green,
          ),
        ),
      ],
    );
  }

  String? _getAvatarUrl(String? avatarPath) {
    if (avatarPath == null || avatarPath.isEmpty) return null;
    if (avatarPath.startsWith('http')) return avatarPath;

    // Get base URL without /api suffix
    final baseUrl = ApiConstants.baseUrl.replaceAll('/api', '');
    return '$baseUrl$avatarPath';
  }
}

