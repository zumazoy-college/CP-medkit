import api from './api';

export interface DiagnosisInfo {
  idDiagnosis: number;
  icdCode: string;
  name: string;
  isPrimary?: boolean;
}

export interface TemplateResponse {
  idTemplate: number;
  doctorId: number;
  title: string;
  complaints?: string;
  anamnesis?: string;
  examination?: string;
  recommendations?: string;
  diagnoses: DiagnosisInfo[];
  createdAt: string;
}

export interface TemplateDiagnosisRequest {
  diagnosisId: number;
  isPrimary: boolean;
}

export interface CreateTemplateRequest {
  title: string;
  complaints?: string;
  anamnesis?: string;
  examination?: string;
  recommendations?: string;
  diagnosisIds?: number[];
  diagnoses?: TemplateDiagnosisRequest[];
}

export interface UpdateTemplateRequest {
  title?: string;
  complaints?: string;
  anamnesis?: string;
  examination?: string;
  recommendations?: string;
  diagnosisIds?: number[];
  diagnoses?: TemplateDiagnosisRequest[];
}

const templateService = {
  getMyTemplates: async (): Promise<TemplateResponse[]> => {
    const response = await api.get('/templates/my');
    return response.data;
  },

  getTemplateById: async (id: number): Promise<TemplateResponse> => {
    const response = await api.get(`/templates/${id}`);
    return response.data;
  },

  createTemplate: async (data: CreateTemplateRequest): Promise<TemplateResponse> => {
    const response = await api.post('/templates', data);
    return response.data;
  },

  updateTemplate: async (id: number, data: UpdateTemplateRequest): Promise<TemplateResponse> => {
    const response = await api.put(`/templates/${id}`, data);
    return response.data;
  },

  deleteTemplate: async (id: number): Promise<void> => {
    await api.delete(`/templates/${id}`);
  },
};

export default templateService;
