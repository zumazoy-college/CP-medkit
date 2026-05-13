import apiClient from './client';

export interface Medication {
  idMedication: number;
  title: string;
  activeSubstance: string;
  manufacturer: string;
  form: string;
}

export interface Procedure {
  idProcedure: number;
  title: string;
  description: string;
  duration: number;
}

export interface Analysis {
  idAnalysis: number;
  code: string;
  title: string;
  description: string;
}

export interface CreateMedicationPrescriptionRequest {
  appointmentId: number;
  medicationId: number;
  dosage: string;
  frequency: string;
  duration: number;
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

const prescriptionService = {
  searchMedications: async (query: string): Promise<Medication[]> => {
    const response = await apiClient.get(`/medications/search`, { params: { query } });
    return response.data.content || response.data;
  },

  searchProcedures: async (query: string): Promise<Procedure[]> => {
    const response = await apiClient.get(`/procedures/search`, { params: { query } });
    return response.data.content || response.data;
  },

  searchAnalyses: async (query: string): Promise<Analysis[]> => {
    const response = await apiClient.get(`/analyses/search`, { params: { query } });
    return response.data.content || response.data;
  },

  createMedicationPrescription: async (request: CreateMedicationPrescriptionRequest): Promise<void> => {
    await apiClient.post('/prescriptions/medications', request);
  },

  createProcedurePrescription: async (request: CreateProcedurePrescriptionRequest): Promise<void> => {
    await apiClient.post('/prescriptions/procedures', request);
  },

  createAnalysisPrescription: async (request: CreateAnalysisPrescriptionRequest): Promise<void> => {
    await apiClient.post('/prescriptions/analyses', request);
  },

  deleteMedicationPrescription: async (prescriptionId: number): Promise<void> => {
    await apiClient.delete(`/prescriptions/medications/${prescriptionId}`);
  },

  deleteProcedurePrescription: async (prescriptionId: number): Promise<void> => {
    await apiClient.delete(`/prescriptions/procedures/${prescriptionId}`);
  },

  deleteAnalysisPrescription: async (prescriptionId: number): Promise<void> => {
    await apiClient.delete(`/prescriptions/analyses/${prescriptionId}`);
  },
};

export default prescriptionService;
