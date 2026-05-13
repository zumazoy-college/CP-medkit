import apiClient from './client';
import { Review } from '../types';

export interface ReviewFilters {
  doctorId?: number;
  patientId?: number;
  rating?: number;
  dateFrom?: string;
  dateTo?: string;
}

export interface ReportReviewData {
  reason: string;
  description?: string;
}

class ReviewService {
  async getReviews(filters?: ReviewFilters): Promise<Review[]> {
    const response = await apiClient.get<Review[]>('/reviews', { params: filters });
    return response.data;
  }

  async getDoctorReviews(doctorId: number): Promise<Review[]> {
    const response = await apiClient.get<{ content: Review[] }>(`/doctors/${doctorId}/reviews`);
    return response.data.content;
  }

  async getDoctorRatingStats(doctorId: number): Promise<{
    averageRating: number;
    totalReviews: number;
    distribution: { [key: number]: number };
  }> {
    const response = await apiClient.get(`/doctors/${doctorId}/rating-stats`);
    return response.data;
  }

  async reportReview(reviewId: number, data: ReportReviewData): Promise<void> {
    await apiClient.post(`/reviews/${reviewId}/report`, data);
  }
}

export default new ReviewService();
