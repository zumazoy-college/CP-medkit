import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useDoctorReviews, useDoctorRatingStats } from '../hooks/useReviews';
import reviewService from '../api/review.service';
import Layout from '../components/layout/Layout';
import './ReviewsPage.css';

const ReviewsPage: React.FC = () => {
  const { user } = useAuth();
  const doctorId = user?.doctor?.id || 0;

  const { reviews, loading: reviewsLoading, refetch } = useDoctorReviews(doctorId);
  const { stats, loading: statsLoading } = useDoctorRatingStats(doctorId);

  const [showReportModal, setShowReportModal] = useState(false);
  const [selectedReviewId, setSelectedReviewId] = useState<number | null>(null);
  const [reportReason, setReportReason] = useState('');
  const [reportDescription, setReportDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const renderStars = (rating: number) => {
    return '⭐'.repeat(rating) + '☆'.repeat(5 - rating);
  };

  const handleReport = (reviewId: number) => {
    setSelectedReviewId(reviewId);
    setShowReportModal(true);
  };

  const handleSubmitReport = async () => {
    if (!reportReason.trim()) {
      alert('Укажите причину жалобы');
      return;
    }

    if (!selectedReviewId) return;

    try {
      setSubmitting(true);
      await reviewService.reportReview(selectedReviewId, {
        reason: reportReason,
        description: reportDescription,
      });
      alert('Жалоба отправлена');
      setShowReportModal(false);
      setReportReason('');
      setReportDescription('');
      setSelectedReviewId(null);
    } catch (error) {
      console.error('Error reporting review:', error);
      alert('Ошибка при отправке жалобы');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCloseModal = () => {
    setShowReportModal(false);
    setReportReason('');
    setReportDescription('');
    setSelectedReviewId(null);
  };

  if (reviewsLoading || statsLoading) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center' }}>Загрузка...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="reviews-page">
        <div className="page-header">
          <h1>Отзывы</h1>
        </div>

        {stats && (
          <div className="rating-summary">
            <div className="rating-score">
              <div className="score-number">{stats.averageRating.toFixed(1)}</div>
              <div className="score-stars">{renderStars(Math.round(stats.averageRating))}</div>
              <div className="score-total">{stats.totalReviews} отзывов</div>
            </div>

            <div className="rating-distribution">
              {[5, 4, 3, 2, 1].map((star) => {
                const count = stats.distribution[star] || 0;
                const percentage = stats.totalReviews > 0 ? (count / stats.totalReviews) * 100 : 0;
                return (
                  <div key={star} className="distribution-row">
                    <span className="stars-label">{star} ⭐</span>
                    <div className="distribution-bar">
                      <div
                        className="distribution-fill"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                    <span className="stars-count">{count}</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        <div className="reviews-list">
          {reviews.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#718096' }}>
              Пока нет отзывов
            </div>
          ) : (
            reviews.map((review) => (
              <div key={review.idReview || review.id} className="review-card">
                <div className="review-header">
                  <div>
                    <div className="review-patient">
                      {review.patientName}
                    </div>
                    <div className="review-date">
                      {review.appointmentDate
                        ? new Date(review.appointmentDate).toLocaleDateString('ru-RU')
                        : new Date(review.createdAt).toLocaleDateString('ru-RU')}
                    </div>
                  </div>
                  <div className="review-rating">{renderStars(review.rating)}</div>
                </div>
                <div className="review-comment">{review.comment}</div>
                <button
                  className="btn-report"
                  onClick={() => handleReport(review.idReview || review.id)}
                >
                  Пожаловаться
                </button>
              </div>
            ))
          )}
        </div>

        {showReportModal && (
          <div className="modal-overlay" onClick={handleCloseModal}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Пожаловаться на отзыв</h2>
                <button className="modal-close" onClick={handleCloseModal}>
                  ✕
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Причина жалобы *</label>
                  <input
                    type="text"
                    value={reportReason}
                    onChange={(e) => setReportReason(e.target.value)}
                    placeholder="Укажите причину"
                  />
                </div>
                <div className="form-group">
                  <label>Дополнительная информация</label>
                  <textarea
                    value={reportDescription}
                    onChange={(e) => setReportDescription(e.target.value)}
                    placeholder="Опишите подробнее (необязательно)"
                    rows={4}
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={handleCloseModal}>
                  Отмена
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleSubmitReport}
                  disabled={submitting}
                >
                  {submitting ? 'Отправка...' : 'Отправить жалобу'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default ReviewsPage;
