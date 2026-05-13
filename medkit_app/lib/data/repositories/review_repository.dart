import '../models/review_model.dart';
import '../models/rating_stats_model.dart';
import '../services/api_service.dart';
import '../../core/constants/api_constants.dart';

class ReviewRepository {
  final ApiService _apiService;

  ReviewRepository(this._apiService);

  Future<ReviewModel> createReview({
    required int appointmentId,
    required int rating,
    String? comment,
  }) async {
    final response = await _apiService.post(
      ApiConstants.reviews,
      data: {
        'appointmentId': appointmentId,
        'rating': rating,
        'comment': comment,
      },
    );

    return ReviewModel.fromJson(response.data);
  }

  Future<ReviewModel> updateReview({
    required int reviewId,
    required int rating,
    String? comment,
  }) async {
    final response = await _apiService.put(
      '${ApiConstants.reviews}/$reviewId',
      data: {
        'rating': rating,
        'comment': comment,
      },
    );

    return ReviewModel.fromJson(response.data);
  }

  Future<List<ReviewModel>> getDoctorReviews({
    required int doctorId,
    int page = 0,
    int size = 20,
  }) async {
    final response = await _apiService.get(
      '${ApiConstants.reviews}/doctor/$doctorId',
      queryParameters: {
        'page': page,
        'size': size,
      },
    );

    final content = response.data['content'] as List;
    return content.map((json) => ReviewModel.fromJson(json)).toList();
  }

  Future<RatingStatsModel> getDoctorRatingStats(int doctorId) async {
    final response = await _apiService.get(
      '${ApiConstants.doctors}/$doctorId/rating-stats',
    );

    return RatingStatsModel.fromJson(response.data);
  }

  Future<void> deleteReview(int reviewId) async {
    await _apiService.delete('${ApiConstants.reviews}/$reviewId');
  }
}
