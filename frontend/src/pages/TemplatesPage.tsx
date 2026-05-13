import React, { useState, useEffect } from 'react';
import { useTemplates } from '../hooks/useTemplates';
import templateService from '../services/templateService';
import diagnosisService, { Diagnosis } from '../services/diagnosisService';
import Layout from '../components/layout/Layout';
import { Template } from '../types';
import './TemplatesPage.css';

interface SelectedDiagnosis {
  idDiagnosis: number;
  icdCode: string;
  name: string;
  isPrimary: boolean;
}

const TemplatesPage: React.FC = () => {
  const { templates, loading, refetch } = useTemplates();

  const [showModal, setShowModal] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<Template | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    complaints: '',
    anamnesis: '',
    examination: '',
    recommendations: '',
  });
  const [saving, setSaving] = useState(false);

  // Diagnosis search
  const [diagnosisSearch, setDiagnosisSearch] = useState('');
  const [diagnosisResults, setDiagnosisResults] = useState<Diagnosis[]>([]);
  const [searchingDiagnosis, setSearchingDiagnosis] = useState(false);
  const [selectedDiagnoses, setSelectedDiagnoses] = useState<SelectedDiagnosis[]>([]);

  const handleEdit = (template: Template) => {
    setEditingTemplate(template);
    setFormData({
      name: template.name || template.title || '',
      complaints: template.complaints || '',
      anamnesis: template.anamnesis || '',
      examination: template.examination || template.objectiveData || '',
      recommendations: template.recommendations || '',
    });

    // Load diagnoses from template
    if (template.diagnoses && Array.isArray(template.diagnoses)) {
      const diagnoses = template.diagnoses.map(d => ({
        idDiagnosis: d.idDiagnosis,
        icdCode: d.icdCode,
        name: d.name,
        isPrimary: d.isPrimary || false
      }));
      setSelectedDiagnoses(diagnoses);
    } else {
      setSelectedDiagnoses([]);
    }

    setShowModal(true);
  };

  const handleCreate = () => {
    setEditingTemplate(null);
    setFormData({
      name: '',
      complaints: '',
      anamnesis: '',
      examination: '',
      recommendations: '',
    });
    setSelectedDiagnoses([]);
    setShowModal(true);
  };

  const searchDiagnoses = async (query: string) => {
    if (query.length < 2) {
      setDiagnosisResults([]);
      return;
    }

    try {
      setSearchingDiagnosis(true);
      const response = await diagnosisService.searchDiagnoses(query, 0, 10);
      setDiagnosisResults(response.content || []);
    } catch (err) {
      console.error('Ошибка поиска диагнозов:', err);
    } finally {
      setSearchingDiagnosis(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      searchDiagnoses(diagnosisSearch);
    }, 300);

    return () => clearTimeout(timer);
  }, [diagnosisSearch]);

  const handleAddDiagnosis = (diagnosis: Diagnosis) => {
    // Check if already added
    if (selectedDiagnoses.some(d => d.idDiagnosis === diagnosis.idDiagnosis)) {
      return;
    }

    // If this is the first diagnosis, make it primary by default
    const isPrimary = selectedDiagnoses.length === 0;

    setSelectedDiagnoses([...selectedDiagnoses, {
      idDiagnosis: diagnosis.idDiagnosis,
      icdCode: diagnosis.icdCode,
      name: diagnosis.name,
      isPrimary: isPrimary
    }]);
    setDiagnosisSearch('');
    setDiagnosisResults([]);
  };

  const handleRemoveDiagnosis = (idDiagnosis: number) => {
    setSelectedDiagnoses(selectedDiagnoses.filter(d => d.idDiagnosis !== idDiagnosis));
  };

  const handleSetPrimaryDiagnosis = (idDiagnosis: number) => {
    setSelectedDiagnoses(selectedDiagnoses.map(d => ({
      ...d,
      isPrimary: d.idDiagnosis === idDiagnosis
    })));
  };

  const handleSave = async () => {
    if (!formData.name.trim()) {
      alert('Введите название шаблона');
      return;
    }

    try {
      setSaving(true);
      const diagnoses = selectedDiagnoses.map(d => ({
        diagnosisId: d.idDiagnosis,
        isPrimary: d.isPrimary
      }));

      const requestData = {
        title: formData.name,
        complaints: formData.complaints,
        anamnesis: formData.anamnesis,
        examination: formData.examination,
        recommendations: formData.recommendations,
        diagnoses
      };

      if (editingTemplate) {
        await templateService.updateTemplate(editingTemplate.id || editingTemplate.idTemplate!, requestData);
      } else {
        await templateService.createTemplate(requestData);
      }
      setShowModal(false);
      refetch();
    } catch (error) {
      console.error('Error saving template:', error);
      alert('Ошибка при сохранении шаблона');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Вы уверены, что хотите удалить этот шаблон?')) {
      return;
    }

    try {
      await templateService.deleteTemplate(id);
      refetch();
    } catch (error) {
      console.error('Error deleting template:', error);
      alert('Ошибка при удалении шаблона');
    }
  };

  return (
    <Layout>
      <div className="templates-page">
        <div className="page-header">
          <h1>Шаблоны</h1>
          <button className="btn btn-primary" onClick={handleCreate}>
            + Создать шаблон
          </button>
        </div>

        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center' }}>Загрузка...</div>
        ) : (
          <div className="templates-grid">
            {templates.length === 0 ? (
              <div style={{ padding: '40px', textAlign: 'center', color: '#718096', gridColumn: '1 / -1' }}>
                Нет созданных шаблонов
              </div>
            ) : (
              templates.map((template) => (
                <div key={template.id || template.idTemplate} className="template-card">
                  <div className="template-header">
                    <h3>{template.name || template.title}</h3>
                    <div className="template-actions">
                      <button className="btn-icon" onClick={() => handleEdit(template)}>
                        ✏️
                      </button>
                      <button className="btn-icon" onClick={() => handleDelete(template.id || template.idTemplate!)}>
                        🗑️
                      </button>
                    </div>
                  </div>
                  <div className="template-content">
                    {template.complaints && (
                      <div className="template-section">
                        <strong>Жалобы:</strong> {template.complaints}
                      </div>
                    )}
                    {template.anamnesis && (
                      <div className="template-section">
                        <strong>Анамнез:</strong> {template.anamnesis}
                      </div>
                    )}
                    {(template.examination || template.objectiveData) && (
                      <div className="template-section">
                        <strong>Осмотр:</strong> {template.examination || template.objectiveData}
                      </div>
                    )}
                    {template.diagnoses && template.diagnoses.length > 0 && (
                      <div className="template-section">
                        <strong>Диагнозы:</strong>
                        <div style={{ marginTop: '5px' }}>
                          {template.diagnoses.map((d, idx) => (
                            <div key={idx} style={{ fontSize: '0.9em', color: '#4A5568' }}>
                              {d.icdCode} - {d.name}
                              {d.isPrimary && <span style={{ marginLeft: '8px', padding: '2px 6px', background: '#3182ce', color: 'white', borderRadius: '4px', fontSize: '0.85em' }}>Основной</span>}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                    {template.recommendations && (
                      <div className="template-section">
                        <strong>Рекомендации:</strong> {template.recommendations}
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>{editingTemplate ? 'Редактировать шаблон' : 'Создать шаблон'}</h2>
                <button className="modal-close" onClick={() => setShowModal(false)}>
                  ✕
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Название шаблона *</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="Например: ОРВИ стандарт"
                  />
                </div>
                <div className="form-group">
                  <label>Жалобы</label>
                  <textarea
                    value={formData.complaints}
                    onChange={(e) => setFormData({ ...formData, complaints: e.target.value })}
                    rows={3}
                  />
                </div>
                <div className="form-group">
                  <label>Анамнез</label>
                  <textarea
                    value={formData.anamnesis}
                    onChange={(e) => setFormData({ ...formData, anamnesis: e.target.value })}
                    rows={3}
                  />
                </div>
                <div className="form-group">
                  <label>Объективные данные</label>
                  <textarea
                    value={formData.examination}
                    onChange={(e) => setFormData({ ...formData, examination: e.target.value })}
                    rows={3}
                  />
                </div>
                <div className="form-group">
                  <label>Диагноз (поиск по МКБ-11)</label>
                  <div className="diagnosis-search">
                    <input
                      type="text"
                      placeholder="Введите код или название диагноза..."
                      value={diagnosisSearch}
                      onChange={(e) => setDiagnosisSearch(e.target.value)}
                    />
                    {searchingDiagnosis && <div className="search-loading">Поиск...</div>}
                    {diagnosisResults.length > 0 && (
                      <div className="diagnosis-results">
                        {diagnosisResults.map((diagnosis) => (
                          <div
                            key={diagnosis.idDiagnosis}
                            className="diagnosis-result-item"
                            onClick={() => handleAddDiagnosis(diagnosis)}
                          >
                            <strong>{diagnosis.icdCode}</strong> - {diagnosis.name}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                  {selectedDiagnoses.length > 0 && (
                    <div className="selected-diagnoses" style={{ marginTop: '10px' }}>
                      {selectedDiagnoses.map((diagnosis) => (
                        <div key={diagnosis.idDiagnosis} className="diagnosis-tag">
                          <div style={{ flex: 1 }}>
                            <strong>{diagnosis.icdCode}</strong> - {diagnosis.name}
                            {diagnosis.isPrimary && <span className="primary-badge">Основной</span>}
                          </div>
                          <div style={{ display: 'flex', gap: '8px', marginLeft: '12px' }}>
                            {!diagnosis.isPrimary && (
                              <button
                                type="button"
                                onClick={() => handleSetPrimaryDiagnosis(diagnosis.idDiagnosis)}
                                style={{
                                  padding: '4px 8px',
                                  fontSize: '12px',
                                  background: '#10b981',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '4px',
                                  cursor: 'pointer'
                                }}
                                title="Сделать основным"
                              >
                                Основной
                              </button>
                            )}
                            <button
                              type="button"
                              className="remove-diagnosis"
                              onClick={() => handleRemoveDiagnosis(diagnosis.idDiagnosis)}
                            >
                              ✕
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                <div className="form-group">
                  <label>Рекомендации</label>
                  <textarea
                    value={formData.recommendations}
                    onChange={(e) => setFormData({ ...formData, recommendations: e.target.value })}
                    rows={3}
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setShowModal(false)}>
                  Отмена
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleSave}
                  disabled={saving}
                >
                  {saving ? 'Сохранение...' : 'Сохранить'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default TemplatesPage;
