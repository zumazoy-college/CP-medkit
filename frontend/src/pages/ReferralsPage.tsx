import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useDoctorReferrals } from '../hooks/useReferrals';
import Layout from '../components/layout/Layout';
import './ReferralsPage.css';

const ReferralsPage: React.FC = () => {
  const { user } = useAuth();
  const [filter, setFilter] = useState<'all' | 'active' | 'completed'>('all');

  const doctorId = user?.doctor?.id || 0;
  const filters = filter !== 'all' ? { fromDoctorId: doctorId, status: filter } : { fromDoctorId: doctorId };
  const { referrals, loading } = useDoctorReferrals(doctorId, filters);

  const getStatusLabel = (status: string) => {
    const labels: { [key: string]: string } = {
      active: 'Активно',
      completed: 'Завершено',
      cancelled: 'Отменено',
      expired: 'Истекло',
    };
    return labels[status] || status;
  };

  const getUrgencyLabel = (urgency: string) => {
    const labels: { [key: string]: string } = {
      routine: 'Плановое',
      urgent: 'Срочное',
      emergency: 'Экстренное',
    };
    return labels[urgency] || urgency;
  };

  return (
    <Layout>
      <div className="referrals-page">
        <div className="page-header">
          <h1>Направления</h1>
        </div>

        <div className="filters">
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            Все
          </button>
          <button
            className={`filter-btn ${filter === 'active' ? 'active' : ''}`}
            onClick={() => setFilter('active')}
          >
            Активные
          </button>
          <button
            className={`filter-btn ${filter === 'completed' ? 'active' : ''}`}
            onClick={() => setFilter('completed')}
          >
            Выполненные
          </button>
        </div>

        <div className="referrals-grid">
          {loading ? (
            <div style={{ padding: '40px', textAlign: 'center', gridColumn: '1 / -1' }}>Загрузка...</div>
          ) : referrals.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#718096', gridColumn: '1 / -1' }}>
              Направления не найдены
            </div>
          ) : (
            referrals.map((referral) => (
              <div key={referral.id} className="referral-card">
                <div className="referral-header">
                  <span className="referral-type">{getUrgencyLabel(referral.urgency)}</span>
                  <span className={`referral-status ${referral.status}`}>
                    {getStatusLabel(referral.status)}
                  </span>
                </div>
                <div className="referral-patient">
                  {referral.patient.lastName} {referral.patient.firstName}
                </div>
                <div className="referral-target">
                  К: {referral.toDoctor
                    ? `${referral.toDoctor.lastName} ${referral.toDoctor.firstName}`
                    : referral.toSpecialty || referral.toClinic}
                </div>
                <div className="referral-purpose">{referral.purpose}</div>
                <div className="referral-date">
                  Создано: {new Date(referral.createdAt).toLocaleDateString('ru-RU')}
                </div>
                {referral.validUntil && (
                  <div className="referral-date">
                    Действительно до: {new Date(referral.validUntil).toLocaleDateString('ru-RU')}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </Layout>
  );
};

export default ReferralsPage;
