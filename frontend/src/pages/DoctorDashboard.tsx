import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useTodayAppointments } from '../hooks/useAppointments';
import { useTimer } from '../hooks/useTimer';
import notificationService from '../api/notification.service';
import doctorService from '../api/doctor.service';
import Layout from '../components/layout/Layout';
import { Notification } from '../types';
import './DoctorDashboard.css';

const DoctorDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const doctorId = user?.doctor?.id;

  const { appointments, loading: appointmentsLoading } = useTodayAppointments(doctorId || 0);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [stats, setStats] = useState({ todayAppointments: 0, weekAppointments: 0 });
  const [loading, setLoading] = useState(true);
  const dataFetchedRef = useRef(false);

  useEffect(() => {
    const fetchData = async () => {
      if (!user?.id || !doctorId || dataFetchedRef.current) {
        return;
      }

      try {
        dataFetchedRef.current = true;

        // Загружаем уведомления
        const notifs = await notificationService.getNotifications(user.id);
        setNotifications(notifs.slice(0, 3)); // Показываем только последние 3

        // Загружаем статистику
        const doctorStats = await doctorService.getDoctorStats(doctorId);

        setStats({
          todayAppointments: doctorStats.todayCompletedAppointments,
          weekAppointments: doctorStats.weekCompletedAppointments,
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    if (!appointmentsLoading) {
      fetchData();
    }
  }, [user?.id, doctorId, appointmentsLoading]);

  // Находим текущий прием
  const now = new Date();
  const currentAppointment = appointments.find((apt) => {
    const aptDate = apt.slotDate || apt.dateTime?.split('T')[0];
    const aptTimeStr = apt.startTime || apt.dateTime?.split('T')[1];
    if (!aptDate || !aptTimeStr) return false;

    const aptTime = new Date(`${aptDate}T${aptTimeStr}`);
    const duration = apt.duration || 20;
    const aptEnd = new Date(aptTime.getTime() + duration * 60000);
    return aptTime <= now && now <= aptEnd && apt.status === 'booked';
  });

  // Находим ближайший прием
  const upcomingAppointments = appointments.filter((apt) => {
    const aptDate = apt.slotDate || apt.dateTime?.split('T')[0];
    const aptTimeStr = apt.startTime || apt.dateTime?.split('T')[1];
    if (!aptDate || !aptTimeStr) return false;

    const aptTime = new Date(`${aptDate}T${aptTimeStr}`);
    return aptTime > now && apt.status === 'booked';
  });
  const nextAppointment = upcomingAppointments.length > 0 ? upcomingAppointments[0] : null;

  // Таймеры
  const currentAppointmentEnd = currentAppointment ? (() => {
    const aptDate = currentAppointment.slotDate || currentAppointment.dateTime?.split('T')[0];
    const aptEndTimeStr = currentAppointment.endTime;
    if (!aptDate || !aptEndTimeStr) return null;
    return new Date(`${aptDate}T${aptEndTimeStr}`);
  })() : null;

  const nextAppointmentTime = nextAppointment ? (() => {
    const aptDate = nextAppointment.slotDate || nextAppointment.dateTime?.split('T')[0];
    const aptTimeStr = nextAppointment.startTime || nextAppointment.dateTime?.split('T')[1];
    if (!aptDate || !aptTimeStr) return null;
    return new Date(`${aptDate}T${aptTimeStr}`);
  })() : null;

  const currentTimer = useTimer(currentAppointmentEnd);
  const nextTimer = useTimer(nextAppointmentTime);

  const currentDate = new Date().toLocaleDateString('ru-RU', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  const getAppointmentStatus = (appointment: any) => {
    const now = new Date();
    const aptDate = appointment.slotDate || appointment.dateTime?.split('T')[0];
    const aptTimeStr = appointment.startTime || appointment.dateTime?.split('T')[1];
    if (!aptDate || !aptTimeStr) return 'upcoming';

    const aptTime = new Date(`${aptDate}T${aptTimeStr}`);
    const duration = appointment.duration || 20;
    const aptEnd = new Date(aptTime.getTime() + duration * 60000);

    if (appointment.status === 'cancelled') return 'cancelled';
    if (appointment.status === 'completed') return 'past';
    if (aptTime <= now && now <= aptEnd && appointment.status === 'booked') return 'current';
    if (aptEnd < now) return 'past';
    return 'upcoming';
  };

  const formatAppointmentTime = (appointment: any) => {
    const aptDate = appointment.slotDate || appointment.dateTime?.split('T')[0];
    const aptTimeStr = appointment.startTime || appointment.dateTime?.split('T')[1];
    if (!aptDate || !aptTimeStr) return '';

    const aptTime = new Date(`${aptDate}T${aptTimeStr}`);
    return aptTime.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading || appointmentsLoading) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center' }}>Загрузка...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="dashboard">
        <div className="dashboard-header">
          <h1 className="dashboard-greeting">
            Здравствуйте, {user?.firstName} {user?.middleName || ''}!
          </h1>
          <p className="dashboard-date">{currentDate}</p>
        </div>

        <div className="dashboard-grid">
          <div className="dashboard-main">
            {currentAppointment ? (
              <div
                className="card current-appointment"
                style={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: '#ffffff',
                  border: 'none',
                  boxShadow: '0 8px 24px rgba(102, 126, 234, 0.4)'
                }}
              >
                <h2 className="card-title" style={{ color: '#ffffff' }}>Текущий прием</h2>
                <div className="appointment-patient" style={{ color: '#ffffff' }}>
                  {currentAppointment.patient.firstName} {currentAppointment.patient.lastName}
                </div>
                <div className="appointment-info" style={{ color: '#ffffff' }}>
                  Возраст: {new Date().getFullYear() - new Date(currentAppointment.patient.dateOfBirth).getFullYear()} лет
                </div>
                {currentTimer && (
                  <div className="appointment-timer" style={{ color: '#ffffff' }}>
                    До конца приема осталось {currentTimer}
                  </div>
                )}
                <button
                  className="btn btn-primary"
                  onClick={() => navigate(`/doctor/patients/${currentAppointment.patient.id}?appointmentId=${currentAppointment.idAppointment}&tab=examination`)}
                >
                  Перейти к приему
                </button>
              </div>
            ) : nextAppointment ? (
              <div
                className="card next-appointment"
                style={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: '#ffffff',
                  border: 'none',
                  boxShadow: '0 8px 24px rgba(102, 126, 234, 0.4)'
                }}
              >
                <h2 className="card-title" style={{ color: '#ffffff' }}>Ближайший прием</h2>
                <div className="appointment-patient" style={{ color: '#ffffff' }}>
                  {nextAppointment.patient.firstName} {nextAppointment.patient.lastName}
                </div>
                <div className="appointment-info" style={{ color: '#ffffff' }}>
                  {formatAppointmentTime(nextAppointment)}
                </div>
                {nextTimer && (
                  <div className="appointment-timer" style={{ color: '#ffffff' }}>
                    До начала приема осталось {nextTimer}
                  </div>
                )}
                <button
                  className="btn btn-primary"
                  onClick={() => navigate(`/doctor/patients/${nextAppointment.patient.id}?appointmentId=${nextAppointment.idAppointment}&tab=examination`)}
                >
                  Перейти в карту
                </button>
              </div>
            ) : null}

            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Расписание на сегодня</h2>
              </div>
              <div className="schedule-list">
                {appointments.length === 0 ? (
                  <div style={{ padding: '20px', textAlign: 'center', color: '#718096' }}>
                    На сегодня нет записей
                  </div>
                ) : (
                  [...appointments]
                    .sort((a, b) => {
                      const timeA = a.startTime || a.dateTime?.split('T')[1] || '00:00';
                      const timeB = b.startTime || b.dateTime?.split('T')[1] || '00:00';
                      return timeA.localeCompare(timeB);
                    })
                    .map((appointment) => {
                    const status = getAppointmentStatus(appointment);
                    const tab = 'history';
                    const appointmentId = appointment.id || appointment.idAppointment;
                    return (
                      <div
                        key={appointmentId}
                        className={`schedule-item ${status}`}
                        onClick={() => navigate(`/doctor/patients/${appointment.patient.id}?appointmentId=${appointmentId}&tab=${tab}`)}
                      >
                      <div className="schedule-time">
                        {formatAppointmentTime(appointment)}
                      </div>
                      <div className="schedule-patient">
                        {appointment.patient.firstName} {appointment.patient.lastName}
                      </div>
                      {getAppointmentStatus(appointment) === 'current' && (
                        <span className="schedule-status current">Сейчас</span>
                      )}
                      </div>
                    );
                  }
                ))
              }
              </div>
            </div>
          </div>

          <div className="dashboard-sidebar">
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Последние уведомления</h2>
                {notifications.length > 0 && (
                  <button
                    className="btn-link"
                    onClick={() => navigate('/doctor/notifications')}
                    title="Посмотреть все уведомления"
                  >
                    Все →
                  </button>
                )}
              </div>
              <div className="notifications-list">
                {notifications.length === 0 ? (
                  <div style={{ padding: '20px', textAlign: 'center', color: '#718096' }}>
                    Нет новых уведомлений
                  </div>
                ) : (
                  notifications.map((notif) => {
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
                        className={`notification-card-mini ${notif.isRead ? 'read' : 'unread'}`}
                        onClick={async () => {
                          if (!notif.isRead) {
                            try {
                              await notificationService.markAsRead(notifId);
                              setNotifications(notifications.map(n =>
                                (n.id === notifId || n.idNotification === notifId)
                                  ? { ...n, isRead: true }
                                  : n
                              ));
                            } catch (error) {
                              console.error('Error marking notification as read:', error);
                            }
                          }
                        }}
                      >
                        <div className="notification-content-mini">
                          <div className="notification-header-mini">
                            <div className="notification-message-mini">{notif.message}</div>
                            {!notif.isRead && <span className="unread-dot"></span>}
                          </div>
                          <div className="notification-date-mini">
                            {notif.title} ({formattedDate} в {formattedTime})
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Статистика</h2>
              </div>
              <div className="stats-grid">
                <div className="stat-item">
                  <div className="stat-value">{stats.todayAppointments}</div>
                  <div className="stat-label">Сегодня</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">{stats.weekAppointments}</div>
                  <div className="stat-label">всего за неделю</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default DoctorDashboard;
