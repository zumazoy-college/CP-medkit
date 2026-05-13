import { useState, useEffect } from 'react';
import referralService, { ReferralFilters } from '../api/referral.service';
import { Referral } from '../types';

export const useReferrals = (filters?: ReferralFilters) => {
  const [referrals, setReferrals] = useState<Referral[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReferrals = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await referralService.getReferrals(filters);
      setReferrals(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch referrals');
      console.error('Error fetching referrals:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReferrals();
  }, [JSON.stringify(filters)]);

  return { referrals, loading, error, refetch: fetchReferrals };
};

export const useDoctorReferrals = (doctorId: number, filters?: ReferralFilters) => {
  const [referrals, setReferrals] = useState<Referral[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReferrals = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await referralService.getDoctorReferrals(doctorId, filters);
      setReferrals(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch referrals');
      console.error('Error fetching referrals:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (doctorId) {
      fetchReferrals();
    }
  }, [doctorId, JSON.stringify(filters)]);

  return { referrals, loading, error, refetch: fetchReferrals };
};
