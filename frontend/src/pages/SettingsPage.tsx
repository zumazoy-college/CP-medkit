import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import doctorService from '../api/doctor.service';
import authService from '../api/auth.service';
import Layout from '../components/layout/Layout';
import ImageCropModal from '../components/ImageCropModal';
import './SettingsPage.css';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const SettingsPage: React.FC = () => {
  const { user, updateUser } = useAuth();

  // Функция для получения полного URL аватарки
  const getAvatarUrl = (avatarPath: string | undefined) => {
    if (!avatarPath) return undefined;
    if (avatarPath.startsWith('http')) return avatarPath;
    return `${API_BASE_URL.replace('/api', '')}${avatarPath}`;
  };
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showCropModal, setShowCropModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    middleName: user?.middleName || '',
    phone: user?.phoneNumber || user?.phone || '',
  });

  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [notifications, setNotifications] = useState({
    appointmentBookings: user?.doctor?.notifyBookings ?? true,
    appointmentCancellations: user?.doctor?.notifyCancellations ?? true,
  });

  // Загружаем актуальные данные врача с сервера
  React.useEffect(() => {
    const loadDoctorData = async () => {
      if (!user?.doctor?.id) return;

      try {
        setLoading(true);
        const doctorData = await doctorService.getDoctorById(user.doctor.id);

        // Обновляем пользователя в контексте с актуальными данными
        const updatedUser = {
          ...user,
          firstName: doctorData.firstName,
          lastName: doctorData.lastName,
          middleName: doctorData.middleName,
          phoneNumber: doctorData.phoneNumber,
          phone: doctorData.phoneNumber,
          email: doctorData.email,
          doctor: {
            ...user.doctor,
            ...doctorData,
            avatar: doctorData.avatarUrl,
            notifyBookings: doctorData.notifyBookings,
            notifyCancellations: doctorData.notifyCancellations,
          },
        };
        updateUser(updatedUser);
      } catch (error) {
        console.error('Error loading doctor data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadDoctorData();
  }, []);

  // Обновляем состояние при изменении user
  React.useEffect(() => {
    if (user) {
      const phoneNumber = user.phoneNumber || user.phone || '';
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        middleName: user.middleName || '',
        phone: phoneNumber ? formatPhoneNumber(phoneNumber) : '',
      });

      if (user.doctor) {
        setNotifications({
          appointmentBookings: user.doctor.notifyBookings ?? true,
          appointmentCancellations: user.doctor.notifyCancellations ?? true,
        });
      }
    }
  }, [user]);

  const handleAvatarSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      setSelectedImage(reader.result as string);
      setShowCropModal(true);
    };
    reader.readAsDataURL(file);

    // Reset input value so the same file can be selected again
    e.target.value = '';
  };

  const handleCroppedImage = async (croppedBlob: Blob) => {
    if (!user?.doctor?.id) return;

    try {
      setLoading(true);
      setShowCropModal(false);

      // Convert blob to file
      const file = new File([croppedBlob], 'avatar.jpg', { type: 'image/jpeg' });
      const result = await doctorService.uploadAvatar(user.doctor.id, file);

      // Обновляем пользователя в контексте
      const updatedUser = {
        ...user,
        doctor: {
          ...user.doctor,
          avatar: result.avatarUrl,
        },
      };
      updateUser(updatedUser);
      alert('Фото успешно загружено');
    } catch (error) {
      console.error('Error uploading avatar:', error);
      alert('Ошибка при загрузке фото');
    } finally {
      setLoading(false);
      setSelectedImage(null);
    }
  };

  const handleAvatarDelete = async () => {
    if (!user?.doctor?.id) return;

    if (!window.confirm('Вы уверены, что хотите удалить фото профиля?')) return;

    try {
      setLoading(true);
      await doctorService.deleteAvatar(user.doctor.id);

      // Обновляем пользователя в контексте
      const updatedUser = {
        ...user,
        doctor: {
          ...user.doctor,
          avatar: undefined,
        },
      };
      updateUser(updatedUser);
      alert('Фото успешно удалено');
    } catch (error) {
      console.error('Error deleting avatar:', error);
      alert('Ошибка при удалении фото');
    } finally {
      setLoading(false);
    }
  };

  // Функция для форматирования телефона в +7 (123) 456-78-90
  const formatPhoneNumber = (value: string): string => {
    // Удаляем все нецифровые символы
    const digits = value.replace(/\D/g, '');

    // Если первая цифра не 7, добавляем 7
    let phone = digits.startsWith('7') ? digits : '7' + digits;

    // Ограничиваем до 11 цифр (7 + 10 цифр)
    phone = phone.slice(0, 11);

    // Форматируем: +7 (123) 456-78-90
    if (phone.length <= 1) {
      return '+7';
    } else if (phone.length <= 4) {
      return `+7 (${phone.slice(1)}`;
    } else if (phone.length <= 7) {
      return `+7 (${phone.slice(1, 4)}) ${phone.slice(4)}`;
    } else if (phone.length <= 9) {
      return `+7 (${phone.slice(1, 4)}) ${phone.slice(4, 7)}-${phone.slice(7)}`;
    } else {
      return `+7 (${phone.slice(1, 4)}) ${phone.slice(4, 7)}-${phone.slice(7, 9)}-${phone.slice(9, 11)}`;
    }
  };

  // Функция для получения только цифр из форматированного телефона
  const getPhoneDigits = (formattedPhone: string): string => {
    return formattedPhone.replace(/\D/g, '');
  };

  // Обработчик изменения телефона
  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatPhoneNumber(e.target.value);
    setFormData({ ...formData, phone: formatted });
  };

  const handleSaveProfile = async () => {
    if (!user?.doctor?.id) return;

    try {
      setSaving(true);
      // Преобразуем phone в phoneNumber для бэкенда (только цифры)
      const dataToSend = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        middleName: formData.middleName,
        phoneNumber: getPhoneDigits(formData.phone),
      };
      await doctorService.updateDoctor(user.doctor.id, dataToSend);

      // Обновляем пользователя в контексте
      const updatedUser = {
        ...user,
        firstName: formData.firstName,
        lastName: formData.lastName,
        middleName: formData.middleName,
        phoneNumber: formData.phone,
        phone: formData.phone,
      };
      updateUser(updatedUser);
      alert('Данные успешно сохранены');
    } catch (error) {
      console.error('Error saving profile:', error);
      alert('Ошибка при сохранении данных');
    } finally {
      setSaving(false);
    }
  };

  const handleChangePassword = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert('Пароли не совпадают');
      return;
    }

    // Валидация пароля
    if (passwordData.newPassword.length < 8) {
      alert('Пароль должен содержать минимум 8 символов');
      return;
    }
    if (!/[a-z]/.test(passwordData.newPassword)) {
      alert('Пароль должен содержать хотя бы одну строчную букву');
      return;
    }
    if (!/[A-Z]/.test(passwordData.newPassword)) {
      alert('Пароль должен содержать хотя бы одну заглавную букву');
      return;
    }
    if (!/[0-9]/.test(passwordData.newPassword)) {
      alert('Пароль должен содержать хотя бы одну цифру');
      return;
    }

    try {
      setSaving(true);
      await authService.changePassword(passwordData.oldPassword, passwordData.newPassword);
      alert('Пароль успешно изменен');
      setShowPasswordModal(false);
      setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      console.error('Error changing password:', error);
      alert('Ошибка при изменении пароля. Проверьте правильность текущего пароля.');
    } finally {
      setSaving(false);
    }
  };

  const handleSaveNotifications = async () => {
    if (!user?.doctor?.id) return;

    try {
      setSaving(true);
      const updatedDoctor = await doctorService.updateNotificationSettings(
        user.doctor.id,
        notifications.appointmentCancellations,
        notifications.appointmentBookings
      );

      // Обновляем пользователя в контексте
      const updatedUser = {
        ...user,
        doctor: {
          ...user.doctor,
          notifyCancellations: updatedDoctor.notifyCancellations,
          notifyBookings: updatedDoctor.notifyBookings,
        },
      };
      updateUser(updatedUser);
      alert('Настройки уведомлений сохранены');
    } catch (error) {
      console.error('Error saving notification settings:', error);
      alert('Ошибка при сохранении настроек уведомлений');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout>
      <div className="settings-page">
        <div className="page-header">
          <h1>Настройки</h1>
        </div>

        <div className="settings-section">
          <h2>Личные данные</h2>
          <div className="settings-card">
            <div className="avatar-section">
              <div className="avatar-preview">
                {user?.doctor?.avatar ? (
                  <img src={getAvatarUrl(user.doctor.avatar)} alt="Avatar" />
                ) : (
                  <div className="avatar-placeholder">👤</div>
                )}
              </div>
              <div className="avatar-actions">
                <label className="btn btn-secondary" style={{ cursor: 'pointer' }}>
                  {loading ? 'Загрузка...' : 'Загрузить фото'}
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarSelect}
                    style={{ display: 'none' }}
                    disabled={loading}
                  />
                </label>
                {user?.doctor?.avatar && (
                  <button
                    className="btn btn-secondary"
                    onClick={handleAvatarDelete}
                    disabled={loading}
                  >
                    Удалить фото
                  </button>
                )}
              </div>
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label>Фамилия</label>
                <input
                  type="text"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Имя</label>
                <input
                  type="text"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Отчество</label>
                <input
                  type="text"
                  value={formData.middleName}
                  onChange={(e) => setFormData({ ...formData, middleName: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Специализация</label>
                <input type="text" value={user?.doctor?.specialization || ''} disabled />
                <small>Редактируется только администратором</small>
              </div>
              <div className="form-group">
                <label>Телефон</label>
                <input
                  type="tel"
                  value={formData.phone}
                  onChange={handlePhoneChange}
                  placeholder="+7 (___) ___-__-__"
                />
              </div>
            </div>

            <div className="form-actions">
              <button
                className="btn btn-primary"
                onClick={handleSaveProfile}
                disabled={saving}
              >
                {saving ? 'Сохранение...' : 'Сохранить изменения'}
              </button>
            </div>
          </div>
        </div>

        <div className="settings-section">
          <h2>Безопасность</h2>
          <div className="settings-card">
            <div className="security-item">
              <div>
                <div className="security-title">Email</div>
                <div className="security-description">{user?.email}</div>
              </div>
            </div>
            <div className="security-item">
              <div>
                <div className="security-title">Пароль</div>
                <div className="security-description">Изменить пароль для входа в систему</div>
              </div>
              <button className="btn btn-secondary" onClick={() => setShowPasswordModal(true)}>
                Изменить
              </button>
            </div>
          </div>
        </div>

        <div className="settings-section">
          <h2>Уведомления</h2>
          <div className="settings-card">
            <div className="notification-item">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={notifications.appointmentBookings}
                  onChange={(e) =>
                    setNotifications({ ...notifications, appointmentBookings: e.target.checked })
                  }
                />
                <div>
                  <div className="notification-title">Уведомления о записи пациента на прием</div>
                  <div className="notification-description">
                    Получать уведомления когда пациент записывается на прием
                  </div>
                </div>
              </label>
            </div>
            <div className="notification-item">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={notifications.appointmentCancellations}
                  onChange={(e) =>
                    setNotifications({ ...notifications, appointmentCancellations: e.target.checked })
                  }
                />
                <div>
                  <div className="notification-title">Уведомления об отмене записи пациентом</div>
                  <div className="notification-description">
                    Получать уведомления когда пациент отменяет запись
                  </div>
                </div>
              </label>
            </div>
            <div className="form-actions">
              <button
                className="btn btn-primary"
                onClick={handleSaveNotifications}
              >
                Сохранить настройки
              </button>
            </div>
          </div>
        </div>

        {showPasswordModal && (
          <div className="modal-overlay" onClick={() => setShowPasswordModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Изменить пароль</h2>
                <button className="modal-close" onClick={() => setShowPasswordModal(false)}>
                  ✕
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Текущий пароль</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showOldPassword ? 'text' : 'password'}
                      value={passwordData.oldPassword}
                      onChange={(e) =>
                        setPasswordData({ ...passwordData, oldPassword: e.target.value })
                      }
                      style={{ paddingRight: '40px' }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowOldPassword(!showOldPassword)}
                      style={{
                        position: 'absolute',
                        right: '8px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        padding: '4px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      {showOldPassword ? (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                          <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                      ) : (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                          <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                      )}
                    </button>
                  </div>
                </div>
                <div className="form-group">
                  <label>Новый пароль</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showNewPassword ? 'text' : 'password'}
                      value={passwordData.newPassword}
                      onChange={(e) =>
                        setPasswordData({ ...passwordData, newPassword: e.target.value })
                      }
                      style={{ paddingRight: '40px' }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      style={{
                        position: 'absolute',
                        right: '8px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        padding: '4px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      {showNewPassword ? (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                          <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                      ) : (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                          <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                      )}
                    </button>
                  </div>
                </div>
                <div className="form-group">
                  <label>Подтвердите новый пароль</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      value={passwordData.confirmPassword}
                      onChange={(e) =>
                        setPasswordData({ ...passwordData, confirmPassword: e.target.value })
                      }
                      style={{ paddingRight: '40px' }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      style={{
                        position: 'absolute',
                        right: '8px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        padding: '4px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      {showConfirmPassword ? (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                          <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                      ) : (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                          <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                      )}
                    </button>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowPasswordModal(false)}
                >
                  Отмена
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleChangePassword}
                  disabled={saving}
                >
                  {saving ? 'Сохранение...' : 'Изменить пароль'}
                </button>
              </div>
            </div>
          </div>
        )}

        {showCropModal && selectedImage && (
          <ImageCropModal
            imageSrc={selectedImage}
            onComplete={handleCroppedImage}
            onCancel={() => {
              setShowCropModal(false);
              setSelectedImage(null);
            }}
          />
        )}
      </div>
    </Layout>
  );
};

export default SettingsPage;
