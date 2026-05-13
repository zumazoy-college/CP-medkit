import apiClient from './client';
import { Notification } from '../types';

export interface NotificationSettings {
  emailNotifications: boolean;
  smsNotifications: boolean;
  pushNotifications: boolean;
  appointmentReminders: boolean;
  appointmentChanges: boolean;
  newMessages: boolean;
  systemUpdates: boolean;
}

class NotificationService {
  async getNotifications(userId: number): Promise<Notification[]> {
    const response = await apiClient.get<{ content: Notification[] }>('/notifications');
    return response.data.content || [];
  }

  async getUnreadCount(userId: number): Promise<number> {
    const response = await apiClient.get<Notification[]>('/notifications/unread');
    return response.data.length;
  }

  async markAsRead(notificationId: number): Promise<void> {
    await apiClient.put(`/notifications/${notificationId}/read`);
  }

  async markAllAsRead(userId: number): Promise<void> {
    await apiClient.put('/notifications/read-all');
  }

  async deleteNotification(notificationId: number): Promise<void> {
    await apiClient.delete(`/notifications/${notificationId}`);
  }

  async getNotificationSettings(userId: number): Promise<NotificationSettings> {
    const response = await apiClient.get<NotificationSettings>(
      `/users/${userId}/notification-settings`
    );
    return response.data;
  }

  async updateNotificationSettings(
    userId: number,
    settings: Partial<NotificationSettings>
  ): Promise<NotificationSettings> {
    const response = await apiClient.patch<NotificationSettings>(
      `/users/${userId}/notification-settings`,
      settings
    );
    return response.data;
  }
}

export default new NotificationService();
