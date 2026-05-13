import '../models/favorite_model.dart';
import '../services/api_service.dart';
import '../../core/constants/api_constants.dart';

class FavoriteRepository {
  final ApiService _apiService;

  FavoriteRepository(this._apiService);

  Future<FavoriteModel?> toggleFavorite(int doctorId) async {
    final response = await _apiService.post(
      '${ApiConstants.favorites}/doctor/$doctorId',
    );

    // If response is 204 No Content, doctor was removed from favorites
    if (response.statusCode == 204) {
      return null;
    }

    // If response is 201 Created, doctor was added to favorites
    return FavoriteModel.fromJson(response.data);
  }

  Future<List<FavoriteModel>> getMyFavorites() async {
    final response = await _apiService.get(ApiConstants.myFavorites);
    final list = response.data as List;
    return list.map((json) => FavoriteModel.fromJson(json)).toList();
  }
}
