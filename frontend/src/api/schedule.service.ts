import apiClient from './client';

export interface Schedule {
  idSchedule: number;
  doctorId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  lunchStart?: string;
  lunchEnd?: string;
  appointmentDuration: number;
  effectiveFrom?: string;
  effectiveTo?: string;
  isActive: boolean;
}

export interface CreateScheduleData {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  lunchStart?: string;
  lunchEnd?: string;
  appointmentDuration: number;
  effectiveFrom?: string;
  effectiveTo?: string;
}

export interface ScheduleException {
  idException: number;
  doctorId: number;
  reason: string;
  startDate: string;
  endDate: string;
}

export interface CreateScheduleExceptionData {
  reason: string;
  startDate: string;
  endDate: string;
  forceBlock?: boolean;
}

export interface AppointmentSlot {
  idSlot: number;
  doctorId: number;
  doctorName: string;
  patientId?: number;
  patientName?: string;
  appointmentId?: number;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: string;
  cancellationReason?: string;
  createdAt: string;
}

export interface GenerateSlotsData {
  startDate: string;
  endDate: string;
  slotDurationMinutes: number;
}

export interface BatchUpdateScheduleData {
  effectiveFrom: string;
  effectiveTo?: string;
  schedules: CreateScheduleData[];
}

class ScheduleService {
  // Получить расписание врача
  async getDoctorSchedule(doctorId: number): Promise<Schedule[]> {
    const response = await apiClient.get<Schedule[]>(`/schedules/doctor/${doctorId}`);
    return response.data;
  }

  // Получить мое расписание (текущего врача)
  async getMySchedule(): Promise<Schedule[]> {
    const response = await apiClient.get<Schedule[]>('/schedules/my');
    return response.data;
  }

  // Создать расписание
  async createSchedule(data: CreateScheduleData): Promise<Schedule> {
    const response = await apiClient.post<Schedule>('/schedules', data);
    return response.data;
  }

  // Обновить расписание
  async updateSchedule(scheduleId: number, data: CreateScheduleData): Promise<Schedule> {
    const response = await apiClient.put<Schedule>(`/schedules/${scheduleId}`, data);
    return response.data;
  }

  // Удалить расписание
  async deleteSchedule(scheduleId: number): Promise<void> {
    await apiClient.delete(`/schedules/${scheduleId}`);
  }

  // Пакетное обновление расписаний
  async batchUpdateSchedules(data: BatchUpdateScheduleData): Promise<Schedule[]> {
    const response = await apiClient.post<Schedule[]>('/schedules/batch', data);
    return response.data;
  }

  // Создать исключение (блокировка даты)
  async createScheduleException(data: CreateScheduleExceptionData): Promise<ScheduleException> {
    const response = await apiClient.post<ScheduleException>('/schedules/exceptions', data);
    return response.data;
  }

  // Получить исключения врача
  async getDoctorScheduleExceptions(doctorId: number): Promise<ScheduleException[]> {
    const response = await apiClient.get<ScheduleException[]>(`/schedules/doctor/${doctorId}/exceptions`);
    return response.data;
  }

  // Получить исключения врача за период
  async getDoctorScheduleExceptionsByDateRange(
    doctorId: number,
    startDate: string,
    endDate: string
  ): Promise<ScheduleException[]> {
    const response = await apiClient.get<ScheduleException[]>(
      `/schedules/doctor/${doctorId}/exceptions/range`,
      {
        params: { startDate, endDate },
      }
    );
    return response.data;
  }

  // Получить мои исключения за период
  async getMyScheduleExceptionsByDateRange(
    startDate: string,
    endDate: string
  ): Promise<ScheduleException[]> {
    const response = await apiClient.get<ScheduleException[]>(
      '/schedules/my/exceptions/range',
      {
        params: { startDate, endDate },
      }
    );
    return response.data;
  }

  // Удалить исключение
  async deleteScheduleException(exceptionId: number): Promise<void> {
    await apiClient.delete(`/schedules/exceptions/${exceptionId}`);
  }

  // Получить слоты за период
  async getMySlotsInRange(startDate: string, endDate: string): Promise<AppointmentSlot[]> {
    const response = await apiClient.get<AppointmentSlot[]>('/slots/my/range', {
      params: { startDate, endDate },
    });
    return response.data;
  }

  // Генерировать слоты
  async generateSlots(data: GenerateSlotsData): Promise<AppointmentSlot[]> {
    const response = await apiClient.post<AppointmentSlot[]>('/slots/generate', data);
    return response.data;
  }

  // Удалить слот
  async deleteSlot(slotId: number): Promise<void> {
    await apiClient.delete(`/slots/${slotId}`);
  }
}

export default new ScheduleService();
