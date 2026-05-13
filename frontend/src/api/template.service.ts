import apiClient from './client';
import { Template } from '../types';

export interface CreateTemplateData {
  name: string;
  title?: string;
  complaints?: string;
  anamnesis?: string;
  examination?: string;
  diagnosis?: string;
  recommendations?: string;
  category?: string;
  diagnosisIds?: number[];
}

export interface UpdateTemplateData extends Partial<CreateTemplateData> {}

class TemplateService {
  async getTemplates(): Promise<Template[]> {
    const response = await apiClient.get<Template[]>('/templates/my');
    return response.data;
  }

  async getTemplateById(id: number): Promise<Template> {
    const response = await apiClient.get<Template>(`/templates/${id}`);
    return response.data;
  }

  async createTemplate(data: CreateTemplateData): Promise<Template> {
    const requestData = {
      title: data.name || data.title,
      complaints: data.complaints,
      anamnesis: data.anamnesis,
      examination: data.examination,
      recommendations: data.recommendations,
      diagnosisIds: data.diagnosisIds || []
    };
    const response = await apiClient.post<Template>('/templates', requestData);
    return response.data;
  }

  async updateTemplate(id: number, data: UpdateTemplateData): Promise<Template> {
    const requestData = {
      title: data.name || data.title,
      complaints: data.complaints,
      anamnesis: data.anamnesis,
      examination: data.examination,
      recommendations: data.recommendations,
      diagnosisIds: data.diagnosisIds
    };
    const response = await apiClient.put<Template>(`/templates/${id}`, requestData);
    return response.data;
  }

  async deleteTemplate(id: number): Promise<void> {
    await apiClient.delete(`/templates/${id}`);
  }

  async applyTemplate(templateId: number, appointmentId: number): Promise<void> {
    await apiClient.post(`/templates/${templateId}/apply`, { appointmentId });
  }
}

export default new TemplateService();
