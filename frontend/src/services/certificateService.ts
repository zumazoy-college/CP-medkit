import api from './api';

export interface CertificateResponse {
  idCertificate: number;
  appointmentId: number;
  patientId: number;
  patientFullName: string;
  doctorId: number;
  doctorFullName: string;
  doctorSpecialization: string;
  certificateType: string;
  certificateTypeName: string;
  filePath: string;
  validFrom?: string;
  validTo?: string;
  disabilityPeriodFrom?: string;
  disabilityPeriodTo?: string;
  workRestrictions?: string;
  createdAt: string;
}

export interface CreateCertificateRequest {
  appointmentId: number;
  certificateType: 'visit' | 'work_study';
  validFrom?: string;
  validTo?: string;
  disabilityPeriodFrom?: string;
  disabilityPeriodTo?: string;
  workRestrictions?: string;
}

class CertificateService {
  async createCertificate(request: CreateCertificateRequest): Promise<CertificateResponse> {
    const response = await api.post('/certificates', request);
    return response.data;
  }

  async getAppointmentCertificates(appointmentId: number): Promise<CertificateResponse[]> {
    const response = await api.get(`/certificates/appointment/${appointmentId}`);
    return response.data;
  }

  async downloadCertificate(certificateId: number): Promise<Blob> {
    const response = await api.get(`/certificates/${certificateId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  }

  downloadCertificateFile(certificateId: number, fileName: string) {
    this.downloadCertificate(certificateId).then((blob) => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName || `certificate_${certificateId}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    });
  }
}

export default new CertificateService();
