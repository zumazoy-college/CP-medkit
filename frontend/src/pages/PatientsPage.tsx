import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { usePatientSearch } from '../hooks/usePatients';
import patientService from '../api/patient.service';
import Layout from '../components/layout/Layout';
import { Patient, MedicalHistory } from '../types';
import './PatientsPage.css';

const PatientsPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [history, setHistory] = useState<MedicalHistory[]>([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'history' | 'diagnoses' | 'prescriptions'>('history');
  const { results, search } = usePatientSearch();

  useEffect(() => {
    const fetchDoctorPatients = async () => {
      if (!user?.doctor?.id) return;

      try {
        setLoading(true);
        const data = await patientService.getDoctorPatients(user.doctor.id);
        setPatients(data);
      } catch (error) {
        console.error('Error fetching patients:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDoctorPatients();
  }, [user]);

  useEffect(() => {
    if (searchQuery.trim()) {
      search(searchQuery);
    }
  }, [searchQuery, search]);

  useEffect(() => {
    const fetchPatientHistory = async () => {
      if (!selectedPatient) return;

      try {
        const data = await patientService.getPatientHistory(selectedPatient.id);
        setHistory(data);
      } catch (error) {
        console.error('Error fetching patient history:', error);
      }
    };

    fetchPatientHistory();
  }, [selectedPatient]);

  const displayedPatients = searchQuery.trim() ? results : patients;

  const calculateAge = (birthdate: string) => {
    const today = new Date();
    const birth = new Date(birthdate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU');
  };

  const formatDateTime = (dateString: string) => {
    // Parse as local time (not UTC)
    const parts = dateString.split(/[-T:]/);
    const date = new Date(
      parseInt(parts[0]),
      parseInt(parts[1]) - 1,
      parseInt(parts[2]),
      parseInt(parts[3] || '0'),
      parseInt(parts[4] || '0'),
      parseInt(parts[5] || '0')
    );
    return date.toLocaleDateString('ru-RU') + ' ' + date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
  };

  // Filter history items with diagnoses
  const historyWithDiagnoses = history.filter(item => item.diagnosis);

  // Filter history items with prescriptions
  const historyWithPrescriptions = history.filter(item => item.prescriptions);

  return (
    <Layout>
      <div className="patients-page">
        <div className="page-header">
          <h1>Пациенты</h1>
        </div>

        <div className="patients-layout">
          <div className="patients-sidebar">
            <div className="search-box">
              <input
                type="text"
                placeholder="Поиск по ФИО, дате рождения, СНИЛС..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="patients-list">
              {loading ? (
                <div style={{ padding: '20px', textAlign: 'center' }}>Загрузка...</div>
              ) : displayedPatients.length === 0 ? (
                <div style={{ padding: '20px', textAlign: 'center', color: '#718096' }}>
                  {searchQuery ? 'Пациенты не найдены' : 'Нет пациентов'}
                </div>
              ) : (
                displayedPatients.map((patient) => (
                  <div
                    key={patient.id}
                    className={`patient-item ${selectedPatient?.id === patient.id ? 'active' : ''}`}
                    onClick={() => setSelectedPatient(patient)}
                  >
                    <div className="patient-name">
                      {patient.lastName} {patient.firstName} {patient.middleName}
                    </div>
                    <div className="patient-info">
                      <span>
                        Дата рождения: {formatDate(patient.dateOfBirth)}
                      </span>
                    </div>
                    {patient.lastAppointmentDate && (
                      <div className="patient-info">
                        <span>Последний прием: {formatDate(patient.lastAppointmentDate)}</span>
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="patient-details">
            {selectedPatient ? (
              <div>
                <div className="card">
                  <h2>
                    {selectedPatient.lastName} {selectedPatient.firstName} {selectedPatient.middleName}
                  </h2>
                  <div className="patient-meta">
                    <div className="meta-item">
                      <span className="meta-label">Дата рождения:</span>
                      <span className="meta-value">
                        {formatDate(selectedPatient.dateOfBirth)} ({calculateAge(selectedPatient.dateOfBirth)} лет)
                      </span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">Пол:</span>
                      <span className="meta-value">{selectedPatient.gender === 'male' ? 'Мужской' : 'Женский'}</span>
                    </div>
                    <div className="meta-item">
                      <span className="meta-label">СНИЛС:</span>
                      <span className="meta-value">{selectedPatient.snils}</span>
                    </div>
                    {selectedPatient.phone && (
                      <div className="meta-item">
                        <span className="meta-label">Телефон:</span>
                        <span className="meta-value">{selectedPatient.phone}</span>
                      </div>
                    )}
                    {selectedPatient.email && (
                      <div className="meta-item">
                        <span className="meta-label">Email:</span>
                        <span className="meta-value">{selectedPatient.email}</span>
                      </div>
                    )}
                    {selectedPatient.allergies && (
                      <div className="meta-item">
                        <span className="meta-label">Аллергии:</span>
                        <span className="meta-value">
                          {Array.isArray(selectedPatient.allergies) ? selectedPatient.allergies.join(', ') : selectedPatient.allergies}
                        </span>
                      </div>
                    )}
                    {selectedPatient.chronicDiseases && (
                      <div className="meta-item">
                        <span className="meta-label">Хронические заболевания:</span>
                        <span className="meta-value">
                          {Array.isArray(selectedPatient.chronicDiseases) ? selectedPatient.chronicDiseases.join(', ') : selectedPatient.chronicDiseases}
                        </span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="card" style={{ marginTop: '20px' }}>
                  <div className="tabs">
                    <div className="tabs-header">
                      <button
                        className={`tab-button ${activeTab === 'history' ? 'active' : ''}`}
                        onClick={() => setActiveTab('history')}
                      >
                        История посещений
                      </button>
                      <button
                        className={`tab-button ${activeTab === 'diagnoses' ? 'active' : ''}`}
                        onClick={() => setActiveTab('diagnoses')}
                      >
                        Диагнозы
                      </button>
                      <button
                        className={`tab-button ${activeTab === 'prescriptions' ? 'active' : ''}`}
                        onClick={() => setActiveTab('prescriptions')}
                      >
                        Назначения
                      </button>
                    </div>

                    <div className="tab-content">
                      {activeTab === 'history' && (
                        <div className="history-list">
                          {history.length === 0 ? (
                            <div style={{ padding: '40px', textAlign: 'center', color: '#718096' }}>
                              История посещений пуста
                            </div>
                          ) : (
                            history.map((item) => (
                              <div key={item.id} className="history-item">
                                <div className="history-date">{formatDateTime(item.date)}</div>
                                {item.doctor && (
                                  <div style={{ fontSize: '14px', color: '#718096', marginBottom: '8px' }}>
                                    Врач: {item.doctor.lastName} {item.doctor.firstName}
                                    {item.doctor.specialization && ` (${item.doctor.specialization})`}
                                  </div>
                                )}
                                {item.diagnosis && (
                                  <div className="history-diagnosis">
                                    <strong>Диагноз:</strong> {item.diagnosis}
                                  </div>
                                )}
                                {item.prescriptions && (
                                  <div className="history-prescriptions">
                                    <strong>Назначения:</strong> {item.prescriptions}
                                  </div>
                                )}
                                {item.notes && (
                                  <div className="history-prescriptions">
                                    <strong>Рекомендации:</strong> {item.notes}
                                  </div>
                                )}
                                <button
                                  className="btn-link"
                                  onClick={() => navigate(`/doctor/patients/${selectedPatient?.id}?appointmentId=${item.appointmentId}&tab=examination`)}
                                >
                                  Подробнее
                                </button>
                              </div>
                            ))
                          )}
                        </div>
                      )}

                      {activeTab === 'diagnoses' && (
                        <div className="history-list">
                          {historyWithDiagnoses.length === 0 ? (
                            <div style={{ padding: '40px', textAlign: 'center', color: '#718096' }}>
                              Диагнозы отсутствуют
                            </div>
                          ) : (
                            historyWithDiagnoses.map((item) => (
                              <div key={item.id} className="history-item">
                                <div className="history-date">{formatDateTime(item.date)}</div>
                                {item.doctor && (
                                  <div style={{ fontSize: '14px', color: '#718096', marginBottom: '8px' }}>
                                    Врач: {item.doctor.lastName} {item.doctor.firstName}
                                  </div>
                                )}
                                <div className="history-diagnosis">
                                  {item.diagnosis}
                                </div>
                              </div>
                            ))
                          )}
                        </div>
                      )}

                      {activeTab === 'prescriptions' && (
                        <div className="history-list">
                          {historyWithPrescriptions.length === 0 ? (
                            <div style={{ padding: '40px', textAlign: 'center', color: '#718096' }}>
                              Назначения отсутствуют
                            </div>
                          ) : (
                            historyWithPrescriptions.map((item) => (
                              <div key={item.id} className="history-item">
                                <div className="history-date">{formatDateTime(item.date)}</div>
                                {item.doctor && (
                                  <div style={{ fontSize: '14px', color: '#718096', marginBottom: '8px' }}>
                                    Врач: {item.doctor.lastName} {item.doctor.firstName}
                                  </div>
                                )}
                                <div className="history-prescriptions">
                                  {item.prescriptions}
                                </div>
                              </div>
                            ))
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">👤</div>
                <div className="empty-state-text">Выберите пациента из списка</div>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default PatientsPage;
