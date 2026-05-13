import 'package:flutter/foundation.dart';
import '../../data/models/review_model.dart';
import '../../data/models/rating_stats_model.dart';
import '../../data/repositories/review_repository.dart';

class ReviewProvider with ChangeNotifier {
  final ReviewRepository _reviewRepository;

  ReviewProvider(this._reviewRepository);

  List<ReviewModel> _reviews = [];
  RatingStatsModel? _ratingStats;
  bool _isLoading = false;
  String? _error;
  String? _successMessage;

  List<ReviewModel> get reviews => _reviews;
  RatingStatsModel? get ratingStats => _ratingStats;
  bool get isLoading => _isLoading;
  String? get error => _error;
  String? get successMessage => _successMessage;

  Future<void> loadDoctorReviews(int doctorId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _reviews = await _reviewRepository.getDoctorReviews(doctorId: doctorId);
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadDoctorRatingStats(int doctorId) async {
    try {
      _ratingStats = await _reviewRepository.getDoctorRatingStats(doctorId);
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<bool> createReview({
    required int appointmentId,
    required int rating,
    String? comment,
  }) async {
    _isLoading = true;
    _error = null;
    _successMessage = null;
    notifyListeners();

    try {
      await _reviewRepository.createReview(
        appointmentId: appointmentId,
        rating: rating,
        comment: comment,
      );
      _successMessage = 'Отзыв успешно отправлен';
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> updateReview({
    required int reviewId,
    required int rating,
    String? comment,
  }) async {
    _isLoading = true;
    _error = null;
    _successMessage = null;
    notifyListeners();

    try {
      await _reviewRepository.updateReview(
        reviewId: reviewId,
        rating: rating,
        comment: comment,
      );
      _successMessage = 'Отзыв успешно обновлен';
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteReview(int reviewId) async {
    try {
      await _reviewRepository.deleteReview(reviewId);
      _reviews.removeWhere((r) => r.idReview == reviewId);
      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      return false;
    }
  }

  void clearMessages() {
    _error = null;
    _successMessage = null;
    notifyListeners();
  }
}
