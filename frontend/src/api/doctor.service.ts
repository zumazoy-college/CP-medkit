import apiClient from './client';
import { Doctor } from '../types';

export interface DoctorScheduleSettings {
  workingHours: {
    start: string;
    end: string;
  };
  breakTime?: {
    start: string;
    end: string;
  };
  slotDuration: number;
  workingDays: number[];
}

export interface BlockedDate {
  date: string;
  reason?: string;
}

export interface UpdateDoctorData {
  firstName?: string;
  lastName?: string;
  middleName?: string;
  specialization?: string;
  phone?: string;
  email?: string;
  bio?: string;
  education?: string;
  experience?: number;
  languages?: string[];
  avatar?: string;
}

class DoctorService {
  async getDoctorById(id: number): Promise<Doctor> {
    const response = await apiClient.get<Doctor>(`/doctors/${id}`);
    return response.data;
  }

  async updateDoctor(id: number, data: UpdateDoctorData): Promise<Doctor> {
    const response = await apiClient.patch<Doctor>(`/doctors/${id}`, data);
    return response.data;
  }

  async uploadAvatar(id: number, file: File): Promise<{ avatarUrl: string }> {
    const formData = new FormData();
    formData.append('avatar', file);

    const response = await apiClient.post<{ avatarUrl: string }>(
      `/doctors/${id}/avatar`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  }

  async deleteAvatar(id: number): Promise<void> {
    await apiClient.delete(`/doctors/${id}/avatar`);
  }

  async getScheduleSettings(doctorId: number): Promise<DoctorScheduleSettings> {
    const response = await apiClient.get<DoctorScheduleSettings>(
      `/doctors/${doctorId}/schedule-settings`
    );
    return response.data;
  }

  async updateScheduleSettings(
    doctorId: number,
    settings: DoctorScheduleSettings
  ): Promise<DoctorScheduleSettings> {
    const response = await apiClient.put<DoctorScheduleSettings>(
      `/doctors/${doctorId}/schedule-settings`,
      settings
    );
    return response.data;
  }

  async getBlockedDates(doctorId: number): Promise<BlockedDate[]> {
    const response = await apiClient.get<BlockedDate[]>(`/doctors/${doctorId}/blocked-dates`);
    return response.data;
  }

  async blockDate(doctorId: number, data: BlockedDate): Promise<BlockedDate> {
    const response = await apiClient.post<BlockedDate>(
      `/doctors/${doctorId}/blocked-dates`,
      data
    );
    return response.data;
  }

  async unblockDate(doctorId: number, date: string): Promise<void> {
    await apiClient.delete(`/doctors/${doctorId}/blocked-dates/${date}`);
  }

  async getDoctorStats(doctorId: number): Promise<{
    todayCompletedAppointments: number;
    weekCompletedAppointments: number;
  }> {
    const response = await apiClient.get(`/doctors/${doctorId}/stats`);
    return response.data;
  }

  async updateNotificationSettings(doctorId: number, notifyCancellations: boolean, notifyBookings: boolean): Promise<Doctor> {
    const response = await apiClient.patch<Doctor>(
      `/doctors/${doctorId}/notification-settings`,
      { notifyCancellations, notifyBookings }
    );
    return response.data;
  }
}

const doctorService = new DoctorService();
export default doctorService;
