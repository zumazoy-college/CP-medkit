// Типы пользователей и ролей
export interface User {
  id: number;
  idUser?: number;
  email: string;
  lastName: string;
  firstName: string;
  middleName?: string;
  phone?: string;
  phoneNumber?: string;
  role: 'doctor' | 'patient';
  avatarUrl?: string;
  avatar?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  doctor?: Doctor;
  patient?: Patient;
}

export interface Doctor {
  id: number;
  idDoctor?: number;
  userId: number;
  specialization: string;
  rating: number;
  reviewsCount: number;
  office?: string;
  workExperience?: string;
  avatar?: string;
  user?: User;
  firstName?: string;
  lastName?: string;
  middleName?: string;
  notifyCancellations?: boolean;
  notifyBookings?: boolean;
}

export interface Patient {
  id: number;
  idPatient?: number;
  userId: number;
  dateOfBirth: string;
  birthdate?: string;
  gender: 'male' | 'female' | 'other';
  snils?: string;
  phone?: string;
  phoneNumber?: string;
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
  user?: User;
  firstName?: string;
  lastName?: string;
  middleName?: string;
  lastAppointmentDate?: string;
}

// Типы расписания и приемов
export interface Schedule {
  idSchedule: number;
  doctorId: number;
  dayOfWeek: number;
  workStart: string;
  workEnd: string;
  breakStart?: string;
  breakEnd?: string;
  slotDuration: number;
}

export interface AppointmentSlot {
  idSlot: number;
  doctorId: number;
  patientId?: number;
  date: string;
  timeStart: string;
  timeEnd: string;
  isAvailable: boolean;
  isCancelled: boolean;
}

export interface Appointment {
  id: number;
  idAppointment?: number;
  slotId?: number;
  patientId: number;
  doctorId?: number;
  dateTime: string;
  slotDate?: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
  complaints?: string;
  anamnesis?: string;
  objectiveData?: string;
  recommendations?: string;
  status: 'scheduled' | 'confirmed' | 'in_progress' | 'completed' | 'cancelled' | 'booked';
  createdAt: string;
  slot?: AppointmentSlot;
  patient: Patient;
  doctor?: Doctor;
  diagnoses?: AppointmentDiagnosis[];
}

// Типы диагнозов
export interface Diagnosis {
  id: number;
  idDiagnosis?: number;
  code: string;
  icdCode?: string;
  name: string;
  icdName?: string;
  description?: string;
  severity?: 'mild' | 'moderate' | 'severe';
}

export interface AppointmentDiagnosis {
  id: number;
  idAppointmentDiagnosis?: number;
  appointmentId: number;
  diagnosisId: number;
  isPrimary: boolean;
  diagnosis?: Diagnosis;
}

// Типы назначений
export interface Medication {
  id: number;
  idMedication?: number;
  title: string;
  name?: string;
  activeSubstance: string;
  manufacturer?: string;
  form?: string;
}

export interface Prescription {
  id: number;
  idPrescription?: number;
  appointmentId: number;
  patientId: number;
  medications: {
    medicationId: number;
    medication?: Medication;
    dosage: string;
    frequency: string;
    duration: string;
    instructions?: string;
  }[];
  notes?: string;
  status: string;
  createdAt: string;
}

export interface MedicationPrescription {
  id: number;
  idPrescription?: number;
  appointmentId: number;
  medicationId: number;
  duration: number;
  instructions?: string;
  status: string;
  createdAt: string;
  medication?: Medication;
}

export interface Procedure {
  idProcedure: number;
  title: string;
  description?: string;
}

export interface ProcedurePrescription {
  idPrescription: number;
  appointmentId: number;
  procedureId: number;
  instructions?: string;
  status: string;
  createdAt: string;
  procedure?: Procedure;
}

export interface Analysis {
  idAnalysis: number;
  title: string;
  code?: string;
}

export interface AnalysisPrescription {
  idPrescription: number;
  appointmentId: number;
  analysisId: number;
  instructions?: string;
  status: string;
  createdAt: string;
  analysis?: Analysis;
}

// Типы направлений
export interface Referral {
  id: number;
  idReferral?: number;
  appointmentId: number;
  patientId: number;
  fromDoctorId: number;
  toDoctorId?: number;
  toSpecialty?: string;
  toClinic?: string;
  referralType?: 'doctor' | 'analysis' | 'procedure';
  targetDoctorId?: number;
  targetAnalysisId?: number;
  targetProcedureId?: number;
  purpose: string;
  urgency: 'routine' | 'urgent' | 'emergency';
  isUrgent?: boolean;
  status: 'active' | 'completed' | 'cancelled' | 'expired';
  validUntil?: string;
  createdAt: string;
  patient: Patient;
  fromDoctor?: Doctor;
  toDoctor?: Doctor;
  targetDoctor?: Doctor;
  targetAnalysis?: Analysis;
  targetProcedure?: Procedure;
}

// Типы шаблонов
export interface TemplateDiagnosis {
  idDiagnosis: number;
  icdCode: string;
  name: string;
  isPrimary?: boolean;
}

export interface Template {
  id: number;
  idTemplate?: number;
  doctorId: number;
  name: string;
  title?: string;
  complaints?: string;
  anamnesis?: string;
  examination?: string;
  objectiveData?: string;
  diagnosis?: string;
  recommendations?: string;
  category?: string;
  createdAt: string;
  diagnoses?: TemplateDiagnosis[];
}

// Типы отзывов
export interface Review {
  id: number;
  idReview?: number;
  patientId?: number;
  patientName: string;
  doctorId: number;
  appointmentId: number;
  rating: number;
  comment?: string;
  createdAt: string;
  appointmentDate?: string;
}

// Типы уведомлений
export interface Notification {
  id: number;
  idNotification?: number;
  userId?: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

// История болезни
export interface MedicalHistory {
  id: number;
  patientId: number;
  appointmentId: number;
  date: string;
  diagnosis: string;
  prescriptions?: string;
  notes?: string;
  doctor?: Doctor;
}

// Типы для UI
export interface DashboardStats {
  todayAppointments: number;
  weekAppointments: number;
  currentAppointment?: Appointment;
  nextAppointment?: Appointment;
  todaySchedule: Appointment[];
  recentNotifications: Notification[];
}
