import { useState, useEffect } from 'react';
import reviewService from '../api/review.service';
import { Review } from '../types';

export const useDoctorReviews = (doctorId: number) => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await reviewService.getDoctorReviews(doctorId);
      setReviews(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch reviews');
      console.error('Error fetching reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (doctorId) {
      fetchReviews();
    }
  }, [doctorId]);

  return { reviews, loading, error, refetch: fetchReviews };
};

export const useDoctorRatingStats = (doctorId: number) => {
  const [stats, setStats] = useState<{
    averageRating: number;
    totalReviews: number;
    distribution: { [key: number]: number };
  } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await reviewService.getDoctorRatingStats(doctorId);
      setStats(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch rating stats');
      console.error('Error fetching rating stats:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (doctorId) {
      fetchStats();
    }
  }, [doctorId]);

  return { stats, loading, error, refetch: fetchStats };
};
