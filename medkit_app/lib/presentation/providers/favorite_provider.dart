import 'package:flutter/foundation.dart';
import '../../data/models/favorite_model.dart';
import '../../data/repositories/favorite_repository.dart';

class FavoriteProvider with ChangeNotifier {
  final FavoriteRepository _favoriteRepository;

  FavoriteProvider(this._favoriteRepository);

  List<FavoriteModel> _favorites = [];
  bool _isLoading = false;
  String? _error;

  List<FavoriteModel> get favorites => _favorites;
  bool get isLoading => _isLoading;
  String? get error => _error;

  bool isFavorite(int doctorId) {
    return _favorites.any((f) => f.doctorId == doctorId);
  }

  Future<void> loadFavorites() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _favorites = await _favoriteRepository.getMyFavorites();
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> toggleFavorite(int doctorId) async {
    try {
      final result = await _favoriteRepository.toggleFavorite(doctorId);

      if (result == null) {
        // Removed from favorites
        _favorites.removeWhere((f) => f.doctorId == doctorId);
      } else {
        // Added to favorites
        _favorites.add(result);
      }

      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
