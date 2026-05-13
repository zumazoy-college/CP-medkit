import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import '../../providers/review_provider.dart';

class CreateReviewScreen extends StatefulWidget {
  final int appointmentId;
  final String doctorName;
  final DateTime appointmentDate;

  const CreateReviewScreen({
    super.key,
    required this.appointmentId,
    required this.doctorName,
    required this.appointmentDate,
  });

  @override
  State<CreateReviewScreen> createState() => _CreateReviewScreenState();
}

class _CreateReviewScreenState extends State<CreateReviewScreen> {
  final _commentController = TextEditingController();
  double _rating = 5.0;

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _submitReview() async {
    final success = await context.read<ReviewProvider>().createReview(
          appointmentId: widget.appointmentId,
          rating: _rating.toInt(),
          comment: _commentController.text.trim().isEmpty
              ? null
              : _commentController.text.trim(),
        );

    if (success && mounted) {
      Navigator.pop(context, true);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Отзыв успешно отправлен')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Оставить отзыв'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Как прошел прием?',
              style: Theme.of(context).textTheme.headlineSmall,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              widget.doctorName,
              style: Theme.of(context).textTheme.titleMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            Text(
              'Оцените врача',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 16),
            Center(
              child: RatingBar.builder(
                initialRating: _rating,
                minRating: 1,
                direction: Axis.horizontal,
                allowHalfRating: false,
                itemCount: 5,
                itemSize: 48,
                itemBuilder: (context, _) => const Icon(
                  Icons.star,
                  color: Colors.amber,
                ),
                onRatingUpdate: (rating) {
                  setState(() {
                    _rating = rating;
                  });
                },
              ),
            ),
            const SizedBox(height: 32),
            Text(
              'Комментарий (необязательно)',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _commentController,
              decoration: const InputDecoration(
                hintText: 'Расскажите о вашем опыте',
              ),
              maxLines: 5,
              maxLength: 1000,
            ),
            const SizedBox(height: 32),
            Consumer<ReviewProvider>(
              builder: (context, reviewProvider, child) {
                if (reviewProvider.error != null) {
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(reviewProvider.error!),
                        backgroundColor: Colors.red,
                      ),
                    );
                    reviewProvider.clearMessages();
                  });
                }

                return ElevatedButton(
                  onPressed: reviewProvider.isLoading ? null : _submitReview,
                  style: ElevatedButton.styleFrom(
                    minimumSize: const Size(double.infinity, 48),
                  ),
                  child: reviewProvider.isLoading
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Отправить отзыв'),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
