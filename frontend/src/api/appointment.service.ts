import apiClient from './client';
import { Appointment } from '../types';

export interface CreateAppointmentData {
  patientId: number;
  doctorId: number;
  dateTime: string;
  duration: number;
  type: string;
  notes?: string;
}

export interface UpdateAppointmentData {
  dateTime?: string;
  duration?: number;
  status?: 'scheduled' | 'confirmed' | 'in_progress' | 'completed' | 'cancelled';
  notes?: string;
}

export interface AppointmentFilters {
  doctorId?: number;
  patientId?: number;
  status?: string;
  dateFrom?: string;
  dateTo?: string;
}

class AppointmentService {
  async getAppointments(filters?: AppointmentFilters): Promise<Appointment[]> {
    const response = await apiClient.get<Appointment[]>('/appointments', {
      params: filters,
    });
    return response.data;
  }

  async getAppointmentById(id: number): Promise<Appointment> {
    const response = await apiClient.get<Appointment>(`/appointments/${id}`);
    return response.data;
  }

  async createAppointment(data: CreateAppointmentData): Promise<Appointment> {
    const response = await apiClient.post<Appointment>('/appointments', data);
    return response.data;
  }

  async updateAppointment(id: number, data: UpdateAppointmentData): Promise<Appointment> {
    const response = await apiClient.patch<Appointment>(`/appointments/${id}`, data);
    return response.data;
  }

  async cancelAppointment(id: number, reason?: string): Promise<void> {
    await apiClient.post(`/appointments/${id}/cancel`, { reason });
  }

  async confirmAppointment(id: number): Promise<Appointment> {
    const response = await apiClient.post<Appointment>(`/appointments/${id}/confirm`);
    return response.data;
  }

  async startAppointment(id: number): Promise<Appointment> {
    const response = await apiClient.post<Appointment>(`/appointments/${id}/start`);
    return response.data;
  }

  async completeAppointment(id: number): Promise<Appointment> {
    const response = await apiClient.post<Appointment>(`/appointments/${id}/complete`);
    return response.data;
  }

  async getDoctorSchedule(doctorId: number, date: string): Promise<Appointment[]> {
    const response = await apiClient.get<Appointment[]>(`/doctors/${doctorId}/schedule`, {
      params: { date },
    });
    return response.data;
  }

  async getTodayAppointments(doctorId: number): Promise<Appointment[]> {
    const today = new Date().toISOString().split('T')[0];
    const response = await apiClient.get<Appointment[]>('/appointments/doctor/my/date', {
      params: { date: today },
    });
    return response.data;
  }

  async getDoctorAppointmentsByDate(date: string): Promise<Appointment[]> {
    const response = await apiClient.get<Appointment[]>('/appointments/doctor/my/date', {
      params: { date },
    });
    return response.data;
  }

  async getPatientHistory(patientId: number): Promise<Appointment[]> {
    const response = await apiClient.get<Appointment[]>(`/patients/${patientId}/history`);
    return response.data;
  }
}

export default new AppointmentService();
