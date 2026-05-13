import api from './api';

export interface PatientInfo {
  id: number;
  firstName: string;
  lastName: string;
  middleName?: string;
  dateOfBirth: string;
  snils: string;
}

export interface DoctorInfo {
  id: number;
  firstName: string;
  lastName: string;
  middleName?: string;
  specialization: string;
}

export interface AppointmentDiagnosis {
  id: number;
  appointmentId: number;
  diagnosisId: number;
  icdCode: string;
  diagnosisName: string;
  isPrimary: boolean;
}

export interface MedicationPrescription {
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

export interface ProcedurePrescription {
  id: number;
  appointmentId: number;
  procedureId: number;
  procedureName: string;
  instructions?: string;
  status: string;
  createdAt: string;
}

export interface AnalysisPrescription {
  id: number;
  appointmentId: number;
  analysisId: number;
  analysisName: string;
  instructions?: string;
  status: string;
  createdAt: string;
}

export interface FileInfo {
  id: number;
  appointmentId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadedAt: string;
}

export interface DetailedAppointment {
  idAppointment: number;
  slotId: number;
  patient: PatientInfo;
  doctor: DoctorInfo;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: string;
  complaints?: string;
  anamnesis?: string;
  objectiveData?: string;
  recommendations?: string;
  diagnoses: AppointmentDiagnosis[];
  medications: MedicationPrescription[];
  procedures: ProcedurePrescription[];
  analyses: AnalysisPrescription[];
  files: FileInfo[];
  createdAt: string;
}

export interface AppointmentResponse {
  idAppointment: number;
  slotId: number;
  patientId: number;
  patientName: string;
  patient: PatientInfo;
  doctorId: number;
  doctorName: string;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: string;
  createdAt: string;
}

export interface UpdateAppointmentRequest {
  complaints?: string;
  anamnesis?: string;
  objectiveData?: string;
  recommendations?: string;
}

const appointmentService = {
  getAppointmentById: async (appointmentId: number): Promise<DetailedAppointment> => {
    const response = await api.get(`/appointments/${appointmentId}`);
    return response.data;
  },

  updateAppointment: async (
    appointmentId: number,
    data: UpdateAppointmentRequest
  ): Promise<DetailedAppointment> => {
    const response = await api.put(`/appointments/${appointmentId}`, data);
    return response.data;
  },

  completeAppointment: async (appointmentId: number): Promise<AppointmentResponse> => {
    const response = await api.post(`/appointments/${appointmentId}/complete`);
    return response.data;
  },

  getDoctorAppointments: async (page: number = 0, size: number = 10) => {
    const response = await api.get('/appointments/doctor/my', {
      params: { page, size },
    });
    return response.data;
  },

  getDoctorAppointmentsByDate: async (date: string): Promise<AppointmentResponse[]> => {
    const response = await api.get('/appointments/doctor/my/date', {
      params: { date },
    });
    return response.data;
  },

  getPatientHistory: async (patientId: number): Promise<DetailedAppointment[]> => {
    const response = await api.get(`/patients/${patientId}/history`);
    return response.data;
  },
};

export default appointmentService;
