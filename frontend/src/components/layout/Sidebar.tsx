import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Sidebar.css';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Sidebar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  // Функция для получения полного URL аватарки
  const getAvatarUrl = (avatarPath: string | undefined) => {
    if (!avatarPath) return undefined;
    if (avatarPath.startsWith('http')) return avatarPath;
    return `${API_BASE_URL.replace('/api', '')}${avatarPath}`;
  };

  const menuItems = [
    { path: '/doctor/dashboard', label: 'Главная', icon: '🏠' },
    { path: '/doctor/schedule', label: 'Расписание', icon: '📅' },
    { path: '/doctor/patients', label: 'Пациенты', icon: '👥' },
    { path: '/doctor/templates', label: 'Шаблоны', icon: '📝' },
    { path: '/doctor/reviews', label: 'Отзывы', icon: '⭐' },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  const firstInitial = user.firstName ? user.firstName.charAt(0) + '.' : '';
  const middleInitial = user.middleName ? user.middleName.charAt(0) + '.' : '';
  const doctorName = `${user.lastName} ${firstInitial}${middleInitial}`;
  const specialization = user.doctor?.specialization || 'Врач';
  const avatarUrl = getAvatarUrl(user.doctor?.avatar || user.avatarUrl);

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2>МЕДКИТ</h2>
      </div>

      <nav className="sidebar-nav">
        {menuItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`sidebar-item ${location.pathname === item.path ? 'active' : ''}`}
          >
            <span className="sidebar-icon">{item.icon}</span>
            <span className="sidebar-label">{item.label}</span>
          </Link>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-profile">
          <div className="profile-avatar">
            {avatarUrl ? (
              <img src={avatarUrl} alt="Avatar" />
            ) : (
              <div className="avatar-placeholder">👤</div>
            )}
          </div>
          <div className="profile-info">
            <div className="profile-name">{doctorName}</div>
            <div className="profile-spec">{specialization}</div>
          </div>
          <Link to="/doctor/settings" className="profile-settings">
            ⚙️
          </Link>
        </div>
        <button onClick={handleLogout} className="logout-button">
          Выйти
        </button>
      </div>
    </div>
  );
};

export default Sidebar;
