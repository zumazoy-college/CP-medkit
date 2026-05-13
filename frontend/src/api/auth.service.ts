import apiClient from './client';
import { User } from '../types';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  userId: number;
  doctorId?: number;
  email: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  role: 'doctor' | 'patient';
}

export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: 'doctor' | 'patient';
  phone?: string;
}

class AuthService {
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', {
      ...credentials,
      expectedRole: 'doctor', // Веб-версия только для врачей
    });

    if (response.data.token) {
      localStorage.setItem('accessToken', response.data.token);

      // Дополнительная проверка роли на клиенте
      if (response.data.role.toLowerCase() !== 'doctor') {
        throw new Error('Доступ запрещен. Используйте мобильное приложение для входа как пациент.');
      }

      // Создаем объект пользователя из ответа
      const user: User = {
        id: response.data.userId,
        email: response.data.email,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        middleName: response.data.middleName,
        role: response.data.role,
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      // Если это врач, загружаем информацию о враче
      if (response.data.role === 'doctor' && response.data.doctorId) {
        try {
          const doctorResponse = await apiClient.get(`/doctors/${response.data.doctorId}`);
          user.doctor = {
            id: doctorResponse.data.idDoctor,
            userId: doctorResponse.data.userId,
            specialization: doctorResponse.data.specialization,
            rating: doctorResponse.data.rating,
            reviewsCount: doctorResponse.data.reviewsCount,
            office: doctorResponse.data.office,
            workExperience: doctorResponse.data.workExperience,
            avatar: doctorResponse.data.avatarUrl,
            firstName: doctorResponse.data.firstName,
            lastName: doctorResponse.data.lastName,
            middleName: doctorResponse.data.middleName,
          };
        } catch (error) {
          console.error('Error loading doctor info:', error);
        }
      }

      localStorage.setItem('user', JSON.stringify(user));
    }

    return response.data;
  }

  async register(data: RegisterData): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/register', data);

    if (response.data.token) {
      localStorage.setItem('accessToken', response.data.token);

      // Создаем объект пользователя из ответа
      const user: User = {
        id: response.data.userId,
        email: response.data.email,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        middleName: response.data.middleName,
        role: response.data.role,
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      localStorage.setItem('user', JSON.stringify(user));
    }

    return response.data;
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
  }

  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;

    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  async refreshToken(): Promise<string> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await apiClient.post<{ access: string }>('/auth/refresh', {
      refresh: refreshToken,
    });

    localStorage.setItem('accessToken', response.data.access);
    return response.data.access;
  }

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await apiClient.post('/auth/change-password', {
      old_password: oldPassword,
      new_password: newPassword,
    });
  }

  async forgotPassword(email: string): Promise<void> {
    await apiClient.post('/auth/forgot-password', { email });
  }

  async verifyResetCode(email: string, code: string): Promise<void> {
    await apiClient.post('/auth/verify-reset-code', { email, code });
  }

  async resetPassword(email: string, code: string, newPassword: string): Promise<void> {
    await apiClient.post('/auth/reset-password', { email, code, newPassword });
  }
}

export default new AuthService();
