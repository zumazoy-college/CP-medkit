import api from './api';

export interface Diagnosis {
  idDiagnosis: number;
  icdCode: string;
  name: string;
}

export interface AppointmentDiagnosisResponse {
  id: number;
  appointmentId: number;
  diagnosisId: number;
  icdCode: string;
  diagnosisName: string;
  isPrimary: boolean;
}

export interface AddDiagnosisRequest {
  appointmentId: number;
  diagnosisId: number;
  isPrimary: boolean;
}

const diagnosisService = {
  searchDiagnoses: async (query: string, page: number = 0, size: number = 10) => {
    const response = await api.get('/diagnoses/search', {
      params: { query, page, size },
    });
    return response.data;
  },

  getDiagnosisByCode: async (icdCode: string): Promise<Diagnosis> => {
    const response = await api.get(`/diagnoses/code/${icdCode}`);
    return response.data;
  },

  addDiagnosisToAppointment: async (
    data: AddDiagnosisRequest
  ): Promise<AppointmentDiagnosisResponse> => {
    const response = await api.post('/diagnoses/appointment', data);
    return response.data;
  },

  getAppointmentDiagnoses: async (
    appointmentId: number
  ): Promise<AppointmentDiagnosisResponse[]> => {
    const response = await api.get(`/diagnoses/appointment/${appointmentId}`);
    return response.data;
  },

  removeDiagnosisFromAppointment: async (appointmentDiagnosisId: number): Promise<void> => {
    await api.delete(`/diagnoses/appointment/${appointmentDiagnosisId}`);
  },

  setPrimaryDiagnosis: async (appointmentDiagnosisId: number): Promise<AppointmentDiagnosisResponse> => {
    const response = await api.put(`/diagnoses/appointment/${appointmentDiagnosisId}/primary`);
    return response.data;
  },
};

export default diagnosisService;
