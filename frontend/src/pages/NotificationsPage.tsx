import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import notificationService from '../api/notification.service';
import Layout from '../components/layout/Layout';
import { Notification } from '../types';
import './NotificationsPage.css';

const NotificationsPage: React.FC = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchNotifications = async () => {
      if (!user?.id) return;

      try {
        const notifs = await notificationService.getNotifications(user.id);
        setNotifications(notifs);
      } catch (error) {
        console.error('Error fetching notifications:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchNotifications();
  }, [user?.id]);

  const handleMarkAsRead = async (notificationId: number) => {
    try {
      await notificationService.markAsRead(notificationId);
      setNotifications(notifications.map(n =>
        n.id === notificationId || n.idNotification === notificationId
          ? { ...n, isRead: true }
          : n
      ));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (!user?.id) return;

    try {
      await notificationService.markAllAsRead(user.id);
      setNotifications(notifications.map(n => ({ ...n, isRead: true })));
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center' }}>Загрузка...</div>
      </Layout>
    );
  }

  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <Layout>
      <div className="notifications-page">
        <div className="notifications-header">
          <div>
            <h1 className="notifications-title">Уведомления</h1>
            {unreadCount > 0 && (
              <p className="notifications-subtitle">
                Непрочитанных: {unreadCount}
              </p>
            )}
          </div>
          {notifications.length > 0 && unreadCount > 0 && (
            <button className="btn btn-secondary" onClick={handleMarkAllAsRead}>
              Отметить все как прочитанные
            </button>
          )}
        </div>

        <div className="notifications-container">
          {notifications.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">🔔</div>
              <div className="empty-state-text">Нет уведомлений</div>
            </div>
          ) : (
            <div className="notifications-list-full">
              {notifications.map((notif) => {
                const notifDate = new Date(notif.createdAt);
                const formattedDate = notifDate.toLocaleDateString('ru-RU', {
                  day: 'numeric',
                  month: 'long'
                });
                const formattedTime = notifDate.toLocaleTimeString('ru-RU', {
                  hour: '2-digit',
                  minute: '2-digit'
                });
                const notifId = notif.id || notif.idNotification;

                return (
                  <div
                    key={notifId}
                    className={`notification-card ${notif.isRead ? 'read' : 'unread'}`}
                    onClick={() => !notif.isRead && handleMarkAsRead(notifId)}
                  >
                    <div className="notification-content">
                      <div className="notification-header-row">
                        <div className="notification-message">{notif.message}</div>
                        {!notif.isRead && <span className="unread-badge">Новое</span>}
                      </div>
                      <div className="notification-date">
                        {notif.title} ({formattedDate} в {formattedTime})
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default NotificationsPage;
