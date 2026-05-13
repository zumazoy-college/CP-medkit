import apiClient from './client';
import { Patient, MedicalHistory } from '../types';

export interface CreatePatientData {
  firstName: string;
  lastName: string;
  middleName?: string;
  dateOfBirth: string;
  gender: 'male' | 'female' | 'other';
  phone: string;
  email?: string;
  address?: string;
  bloodType?: string;
  allergies?: string[];
  chronicDiseases?: string[];
  emergencyContact?: {
    name: string;
    phone: string;
    relationship: string;
  };
}

export interface UpdatePatientData extends Partial<CreatePatientData> {}

export interface PatientSearchParams {
  query?: string;
  dateOfBirth?: string;
  phone?: string;
}

class PatientService {
  async getPatients(params?: PatientSearchParams): Promise<Patient[]> {
    const response = await apiClient.get<Patient[]>('/patients', { params });
    return response.data;
  }

  async getPatientById(id: number): Promise<Patient> {
    const response = await apiClient.get<Patient>(`/patients/${id}`);
    return response.data;
  }

  async createPatient(data: CreatePatientData): Promise<Patient> {
    const response = await apiClient.post<Patient>('/patients', data);
    return response.data;
  }

  async updatePatient(id: number, data: UpdatePatientData): Promise<Patient> {
    const response = await apiClient.patch<Patient>(`/patients/${id}`, data);
    return response.data;
  }

  async deletePatient(id: number): Promise<void> {
    await apiClient.delete(`/patients/${id}`);
  }

  async getPatientHistory(patientId: number): Promise<MedicalHistory[]> {
    const response = await apiClient.get<MedicalHistory[]>(`/patients/${patientId}/history`);
    return response.data;
  }

  async searchPatients(query: string): Promise<Patient[]> {
    const response = await apiClient.get<Patient[]>('/patients/search', {
      params: { q: query },
    });
    return response.data;
  }

  async getDoctorPatients(doctorId: number): Promise<Patient[]> {
    const response = await apiClient.get<Patient[]>(`/doctors/${doctorId}/patients`);
    return response.data;
  }
}

export default new PatientService();
