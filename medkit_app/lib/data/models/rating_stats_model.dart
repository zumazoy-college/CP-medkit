class RatingStatsModel {
  final double averageRating;
  final int totalReviews;
  final Map<int, int> distribution;

  RatingStatsModel({
    required this.averageRating,
    required this.totalReviews,
    required this.distribution,
  });

  factory RatingStatsModel.fromJson(Map<String, dynamic> json) {
    final distributionMap = <int, int>{};
    final distribution = json['distribution'] as Map<String, dynamic>;

    distribution.forEach((key, value) {
      distributionMap[int.parse(key)] = value as int;
    });

    return RatingStatsModel(
      averageRating: (json['averageRating'] ?? 0).toDouble(),
      totalReviews: json['totalReviews'] ?? 0,
      distribution: distributionMap,
    );
  }

  int getCountForRating(int rating) {
    return distribution[rating] ?? 0;
  }

  double getPercentageForRating(int rating) {
    if (totalReviews == 0) return 0.0;
    return (getCountForRating(rating) / totalReviews) * 100;
  }
}
