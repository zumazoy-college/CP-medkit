import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/review_provider.dart';

class EditReviewScreen extends StatefulWidget {
  final int reviewId;
  final String doctorName;
  final DateTime appointmentDate;
  final int initialRating;
  final String? initialComment;

  const EditReviewScreen({
    super.key,
    required this.reviewId,
    required this.doctorName,
    required this.appointmentDate,
    required this.initialRating,
    this.initialComment,
  });

  @override
  State<EditReviewScreen> createState() => _EditReviewScreenState();
}

class _EditReviewScreenState extends State<EditReviewScreen> {
  late int _rating;
  late TextEditingController _commentController;
  final _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
    _rating = widget.initialRating;
    _commentController = TextEditingController(text: widget.initialComment ?? '');
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _submitReview() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    final success = await context.read<ReviewProvider>().updateReview(
          reviewId: widget.reviewId,
          rating: _rating,
          comment: _commentController.text.trim().isEmpty
              ? null
              : _commentController.text.trim(),
        );

    if (success && mounted) {
      Navigator.pop(context, true);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Отзыв успешно обновлен')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Редактировать отзыв'),
      ),
      body: Consumer<ReviewProvider>(
        builder: (context, reviewProvider, child) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    widget.doctorName,
                    style: Theme.of(context).textTheme.headlineSmall,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${widget.appointmentDate.day}.${widget.appointmentDate.month}.${widget.appointmentDate.year}',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Colors.grey[600],
                        ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 32),
                  const Text(
                    'Оценка',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(5, (index) {
                      return IconButton(
                        icon: Icon(
                          index < _rating ? Icons.star : Icons.star_border,
                          size: 40,
                          color: Colors.amber,
                        ),
                        onPressed: () {
                          setState(() {
                            _rating = index + 1;
                          });
                        },
                      );
                    }),
                  ),
                  const SizedBox(height: 24),
                  const Text(
                    'Комментарий (необязательно)',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _commentController,
                    maxLines: 5,
                    maxLength: 1000,
                    decoration: const InputDecoration(
                      hintText: 'Расскажите о вашем опыте...',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 24),
                  if (reviewProvider.error != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: Text(
                        reviewProvider.error!,
                        style: const TextStyle(color: Colors.red),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ElevatedButton(
                    onPressed: reviewProvider.isLoading ? null : _submitReview,
                    child: reviewProvider.isLoading
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Text('Сохранить изменения'),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
