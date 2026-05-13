import api from './api';

export interface CreateMedicationPrescriptionRequest {
  appointmentId: number;
  medicationId: number;
  dosage?: string;
  frequency?: string;
  duration: string;
  instructions?: string;
}

export interface CreateProcedurePrescriptionRequest {
  appointmentId: number;
  procedureId: number;
  instructions?: string;
}

export interface CreateAnalysisPrescriptionRequest {
  appointmentId: number;
  analysisId: number;
  instructions?: string;
}

export interface MedicationPrescriptionResponse {
  id: number;
  appointmentId: number;
  medicationId: number;
  medicationName: string;
  dosage?: string;
  frequency?: string;
  duration: string;
  instructions?: string;
  status: string;
  createdAt: string;
}

export interface ProcedurePrescriptionResponse {
  id: number;
  appointmentId: number;
  procedureId: number;
  procedureName: string;
  instructions?: string;
  status: string;
  createdAt: string;
}

export interface AnalysisPrescriptionResponse {
  id: number;
  appointmentId: number;
  analysisId: number;
  analysisName: string;
  instructions?: string;
  status: string;
  createdAt: string;
}

const prescriptionService = {
  createMedicationPrescription: async (
    data: CreateMedicationPrescriptionRequest
  ): Promise<MedicationPrescriptionResponse> => {
    const response = await api.post('/prescriptions/medications', data);
    return response.data;
  },

  createProcedurePrescription: async (
    data: CreateProcedurePrescriptionRequest
  ): Promise<ProcedurePrescriptionResponse> => {
    const response = await api.post('/prescriptions/procedures', data);
    return response.data;
  },

  createAnalysisPrescription: async (
    data: CreateAnalysisPrescriptionRequest
  ): Promise<AnalysisPrescriptionResponse> => {
    const response = await api.post('/prescriptions/analyses', data);
    return response.data;
  },

  getAppointmentMedicationPrescriptions: async (
    appointmentId: number
  ): Promise<MedicationPrescriptionResponse[]> => {
    const response = await api.get(`/prescriptions/medications/appointment/${appointmentId}`);
    return response.data;
  },

  getAppointmentProcedurePrescriptions: async (
    appointmentId: number
  ): Promise<ProcedurePrescriptionResponse[]> => {
    const response = await api.get(`/prescriptions/procedures/appointment/${appointmentId}`);
    return response.data;
  },

  getAppointmentAnalysisPrescriptions: async (
    appointmentId: number
  ): Promise<AnalysisPrescriptionResponse[]> => {
    const response = await api.get(`/prescriptions/analyses/appointment/${appointmentId}`);
    return response.data;
  },
};

export default prescriptionService;
