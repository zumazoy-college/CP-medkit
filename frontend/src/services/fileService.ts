import api from './api';

export interface FileResponse {
  id: number;
  appointmentId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadedAt: string;
}

const fileService = {
  uploadFile: async (appointmentId: number, file: File): Promise<FileResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('appointmentId', appointmentId.toString());

    const response = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getAppointmentFiles: async (appointmentId: number): Promise<FileResponse[]> => {
    const response = await api.get(`/files/appointment/${appointmentId}`);
    return response.data;
  },

  downloadFile: async (fileId: number): Promise<Blob> => {
    const response = await api.get(`/files/download/${fileId}`, {
      responseType: 'blob',
    });
    return response.data;
  },

  deleteFile: async (fileId: number): Promise<void> => {
    await api.delete(`/files/${fileId}`);
  },
};

export default fileService;
