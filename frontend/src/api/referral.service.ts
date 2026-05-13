import apiClient from './client';
import { Referral } from '../types';

export interface CreateReferralData {
  patientId: number;
  fromDoctorId: number;
  toDoctorId?: number;
  toSpecialty?: string;
  toClinic?: string;
  purpose: string;
  urgency: 'routine' | 'urgent' | 'emergency';
  notes?: string;
  validUntil?: string;
}

export interface UpdateReferralData extends Partial<CreateReferralData> {
  status?: 'active' | 'completed' | 'cancelled' | 'expired';
}

export interface ReferralFilters {
  patientId?: number;
  fromDoctorId?: number;
  toDoctorId?: number;
  status?: string;
  dateFrom?: string;
  dateTo?: string;
}

class ReferralService {
  async getReferrals(filters?: ReferralFilters): Promise<Referral[]> {
    const response = await apiClient.get<Referral[]>('/referrals', { params: filters });
    return response.data;
  }

  async getReferralById(id: number): Promise<Referral> {
    const response = await apiClient.get<Referral>(`/referrals/${id}`);
    return response.data;
  }

  async createReferral(data: CreateReferralData): Promise<Referral> {
    const response = await apiClient.post<Referral>('/referrals', data);
    return response.data;
  }

  async updateReferral(id: number, data: UpdateReferralData): Promise<Referral> {
    const response = await apiClient.patch<Referral>(`/referrals/${id}`, data);
    return response.data;
  }

  async cancelReferral(id: number, reason?: string): Promise<void> {
    await apiClient.post(`/referrals/${id}/cancel`, { reason });
  }

  async completeReferral(id: number): Promise<Referral> {
    const response = await apiClient.post<Referral>(`/referrals/${id}/complete`);
    return response.data;
  }

  async getDoctorReferrals(doctorId: number, filters?: ReferralFilters): Promise<Referral[]> {
    const response = await apiClient.get<Referral[]>(`/doctors/${doctorId}/referrals`, {
      params: filters,
    });
    return response.data;
  }

  async getPatientReferrals(patientId: number): Promise<Referral[]> {
    const response = await apiClient.get<Referral[]>(`/patients/${patientId}/referrals`);
    return response.data;
  }
}

export default new ReferralService();
