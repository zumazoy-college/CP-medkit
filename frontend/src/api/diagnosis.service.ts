import apiClient from './client';
import { Diagnosis, Prescription, Medication } from '../types';

export interface CreateDiagnosisData {
  appointmentId: number;
  patientId: number;
  code: string;
  name: string;
  description?: string;
  severity?: 'mild' | 'moderate' | 'severe';
  notes?: string;
}

export interface UpdateDiagnosisData extends Partial<CreateDiagnosisData> {}

export interface CreatePrescriptionData {
  appointmentId: number;
  patientId: number;
  medications: {
    medicationId: number;
    dosage: string;
    frequency: string;
    duration: string;
    instructions?: string;
  }[];
  notes?: string;
}

export interface SearchDiagnosisParams {
  query: string;
  limit?: number;
}

class DiagnosisService {
  async searchDiagnoses(params: SearchDiagnosisParams): Promise<Diagnosis[]> {
    const response = await apiClient.get<Diagnosis[]>('/diagnoses/search', { params });
    return response.data;
  }

  async createDiagnosis(data: CreateDiagnosisData): Promise<Diagnosis> {
    const response = await apiClient.post<Diagnosis>('/diagnoses', data);
    return response.data;
  }

  async updateDiagnosis(id: number, data: UpdateDiagnosisData): Promise<Diagnosis> {
    const response = await apiClient.patch<Diagnosis>(`/diagnoses/${id}`, data);
    return response.data;
  }

  async deleteDiagnosis(id: number): Promise<void> {
    await apiClient.delete(`/diagnoses/${id}`);
  }

  async getPatientDiagnoses(patientId: number): Promise<Diagnosis[]> {
    const response = await apiClient.get<Diagnosis[]>(`/patients/${patientId}/diagnoses`);
    return response.data;
  }
}

class PrescriptionService {
  async createPrescription(data: CreatePrescriptionData): Promise<Prescription> {
    const response = await apiClient.post<Prescription>('/prescriptions', data);
    return response.data;
  }

  async getPrescriptionById(id: number): Promise<Prescription> {
    const response = await apiClient.get<Prescription>(`/prescriptions/${id}`);
    return response.data;
  }

  async getPatientPrescriptions(patientId: number): Promise<Prescription[]> {
    const response = await apiClient.get<Prescription[]>(`/patients/${patientId}/prescriptions`);
    return response.data;
  }

  async searchMedications(query: string): Promise<Medication[]> {
    const response = await apiClient.get<Medication[]>('/medications/search', {
      params: { q: query },
    });
    return response.data;
  }
}

export const diagnosisService = new DiagnosisService();
export const prescriptionService = new PrescriptionService();
