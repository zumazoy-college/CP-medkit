import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import Layout from '../components/layout/Layout';
import appointmentService, { DetailedAppointment } from '../services/appointmentService';
import diagnosisService, { Diagnosis } from '../services/diagnosisService';
import templateService, { TemplateResponse } from '../services/templateService';
import fileService from '../services/fileService';
import prescriptionService, { Medication, Procedure, Analysis } from '../api/prescription.service';
import certificateService, { CertificateResponse, CreateCertificateRequest } from '../services/certificateService';
import './PatientCard.css';

const PatientCard: React.FC = () => {
  const navigate = useNavigate();
  const { id: patientIdParam } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const appointmentIdParam = searchParams.get('appointmentId');
  const tabParam = searchParams.get('tab') || 'examination';

  const [activeTab, setActiveTab] = useState<'history' | 'examination'>(tabParam as 'history' | 'examination');
  const [appointment, setAppointment] = useState<DetailedAppointment | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Patient history
  const [patientHistory, setPatientHistory] = useState<DetailedAppointment[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  // Form fields
  const [complaints, setComplaints] = useState('');
  const [anamnesis, setAnamnesis] = useState('');
  const [objectiveData, setObjectiveData] = useState('');
  const [recommendations, setRecommendations] = useState('');

  // Diagnosis search
  const [diagnosisSearch, setDiagnosisSearch] = useState('');
  const [diagnosisResults, setDiagnosisResults] = useState<Diagnosis[]>([]);
  const [searchingDiagnosis, setSearchingDiagnosis] = useState(false);

  // Medication search and form
  const [medicationSearch, setMedicationSearch] = useState('');
  const [medicationResults, setMedicationResults] = useState<Medication[]>([]);
  const [searchingMedication, setSearchingMedication] = useState(false);
  const [showMedicationForm, setShowMedicationForm] = useState(false);
  const [selectedMedication, setSelectedMedication] = useState<Medication | null>(null);
  const [medicationDosage, setMedicationDosage] = useState('');
  const [medicationFrequency, setMedicationFrequency] = useState('');
  const [medicationDuration, setMedicationDuration] = useState('');
  const [medicationInstructions, setMedicationInstructions] = useState('');

  // Procedure search and form
  const [procedureSearch, setProcedureSearch] = useState('');
  const [procedureResults, setProcedureResults] = useState<Procedure[]>([]);
  const [searchingProcedure, setSearchingProcedure] = useState(false);
  const [showProcedureForm, setShowProcedureForm] = useState(false);
  const [selectedProcedure, setSelectedProcedure] = useState<Procedure | null>(null);
  const [procedureInstructions, setProcedureInstructions] = useState('');

  // Analysis search and form
  const [analysisSearch, setAnalysisSearch] = useState('');
  const [analysisResults, setAnalysisResults] = useState<Analysis[]>([]);
  const [searchingAnalysis, setSearchingAnalysis] = useState(false);
  const [showAnalysisForm, setShowAnalysisForm] = useState(false);
  const [selectedAnalysis, setSelectedAnalysis] = useState<Analysis | null>(null);
  const [analysisInstructions, setAnalysisInstructions] = useState('');

  // Templates
  const [templates, setTemplates] = useState<TemplateResponse[]>([]);
  const [showTemplateModal, setShowTemplateModal] = useState(false);

  // Files
  const [uploadingFile, setUploadingFile] = useState(false);

  // Timer
  const [timeRemaining, setTimeRemaining] = useState<number>(0);

  // Certificates
  const [showCertificateModal, setShowCertificateModal] = useState(false);
  const [certificateType, setCertificateType] = useState<'visit' | 'work_study'>('visit');
  const [validFrom, setValidFrom] = useState('');
  const [validTo, setValidTo] = useState('');
  const [disabilityPeriodFrom, setDisabilityPeriodFrom] = useState('');
  const [disabilityPeriodTo, setDisabilityPeriodTo] = useState('');
  const [workRestrictions, setWorkRestrictions] = useState('');
  const [creatingCertificate, setCreatingCertificate] = useState(false);
  const [certificates, setCertificates] = useState<CertificateResponse[]>([]);

  const loadPatientHistory = async (patientId: number, excludeAppointmentId?: number) => {
    try {
      setLoadingHistory(true);
      const history = await appointmentService.getPatientHistory(patientId);
      // Exclude current appointment from history
      setPatientHistory(
        excludeAppointmentId
          ? history.filter(h => h.idAppointment !== excludeAppointmentId)
          : history
      );
    } catch (err) {
      console.error('Ошибка загрузки истории:', err);
    } finally {
      setLoadingHistory(false);
    }
  };

  const loadTemplates = async () => {
    try {
      const data = await templateService.getMyTemplates();
      setTemplates(data);
    } catch (err) {
      console.error('Ошибка загрузки шаблонов:', err);
    }
  };

  const loadCertificates = async (appointmentId: number) => {
    try {
      const data = await certificateService.getAppointmentCertificates(appointmentId);
      setCertificates(data);
    } catch (err) {
      console.error('Ошибка загрузки справок:', err);
    }
  };

  const handleCreateCertificate = async () => {
    if (!appointment) return;

    if (certificateType === 'work_study') {
      if (!disabilityPeriodFrom || !disabilityPeriodTo) {
        alert('Для справки 095/у необходимо указать период нетрудоспособности');
        return;
      }
    }

    try {
      setCreatingCertificate(true);
      const request: CreateCertificateRequest = {
        appointmentId: appointment.idAppointment,
        certificateType,
        validFrom: validFrom || undefined,
        validTo: validTo || undefined,
        disabilityPeriodFrom: disabilityPeriodFrom || undefined,
        disabilityPeriodTo: disabilityPeriodTo || undefined,
        workRestrictions: workRestrictions || undefined,
      };

      const newCertificate = await certificateService.createCertificate(request);
      setCertificates([...certificates, newCertificate]);
      setShowCertificateModal(false);

      // Reset form
      setCertificateType('visit');
      setValidFrom('');
      setValidTo('');
      setDisabilityPeriodFrom('');
      setDisabilityPeriodTo('');
      setWorkRestrictions('');

      alert('Справка успешно создана');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка создания справки');
    } finally {
      setCreatingCertificate(false);
    }
  };

  const handleDownloadCertificate = (certificateId: number, certificateTypeName: string) => {
    const fileName = `${certificateTypeName}_${new Date().toISOString().split('T')[0]}.pdf`;
    certificateService.downloadCertificateFile(certificateId, fileName);
  };

  const loadAppointment = useCallback(async (appointmentId: number) => {
    try {
      setLoading(true);
      const data = await appointmentService.getAppointmentById(appointmentId);
      setAppointment(data);
      setComplaints(data.complaints || '');
      setAnamnesis(data.anamnesis || '');
      setObjectiveData(data.objectiveData || '');
      setRecommendations(data.recommendations || '');

      // Load patient history
      if (data.patient.id) {
        loadPatientHistory(data.patient.id, appointmentId);
      }

      // Load certificates
      loadCertificates(appointmentId);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки приема');
    } finally {
      setLoading(false);
    }
  }, []);

  const findAndLoadAppointment = useCallback(async (patientId: number) => {
    try {
      setLoading(true);

      // First, try to find today's appointments for this patient
      const today = new Date().toISOString().split('T')[0];
      const todayAppointments = await appointmentService.getDoctorAppointmentsByDate(today);

      // Find current or next appointment for this patient
      const now = new Date();
      const currentHour = now.getHours();
      const currentMinute = now.getMinutes();
      const currentTimeMinutes = currentHour * 60 + currentMinute;

      const patientAppointmentsToday = todayAppointments.filter(apt => apt.patient.id === patientId);

      // Find appointment that is happening now or next
      let targetAppointment = patientAppointmentsToday.find(apt => {
        const [startHour, startMinute] = apt.startTime.split(':').map(Number);
        const [endHour, endMinute] = apt.endTime.split(':').map(Number);
        const startTimeMinutes = startHour * 60 + startMinute;
        const endTimeMinutes = endHour * 60 + endMinute;

        // Check if appointment is happening now
        return currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes;
      });

      // If no current appointment, find next one
      if (!targetAppointment) {
        targetAppointment = patientAppointmentsToday.find(apt => {
          const [startHour, startMinute] = apt.startTime.split(':').map(Number);
          const startTimeMinutes = startHour * 60 + startMinute;
          return startTimeMinutes > currentTimeMinutes;
        });
      }

      if (targetAppointment) {
        // Found appointment for today - load it
        await loadAppointment(targetAppointment.idAppointment);
        loadTemplates();
      } else {
        // No appointments today - just load patient history
        await loadPatientHistory(patientId);
        setLoading(false);
      }
    } catch (err: any) {
      console.error('Error finding appointment:', err);
      // Fallback - just load patient history
      try {
        await loadPatientHistory(patientId);
      } catch (historyErr) {
        setError('Ошибка загрузки данных пациента');
      }
      setLoading(false);
    }
  }, [loadAppointment]);

  // Load appointment data
  useEffect(() => {
    if (appointmentIdParam) {
      // Explicit appointmentId provided
      loadAppointment(Number(appointmentIdParam));
      loadTemplates();
    } else if (patientIdParam) {
      // No appointmentId - find current or latest appointment
      findAndLoadAppointment(Number(patientIdParam));
    }
  }, [appointmentIdParam, patientIdParam, loadAppointment, findAndLoadAppointment]);

  // Timer countdown
  useEffect(() => {
    if (!appointment) return;

    const calculateTimeRemaining = () => {
      const now = new Date();
      const [hours, minutes] = appointment.endTime.split(':').map(Number);
      const endDateTime = new Date(appointment.slotDate);
      endDateTime.setHours(hours, minutes, 0, 0);

      const diff = endDateTime.getTime() - now.getTime();
      return Math.max(0, Math.floor(diff / 1000 / 60)); // minutes
    };

    setTimeRemaining(calculateTimeRemaining());

    const interval = setInterval(() => {
      setTimeRemaining(calculateTimeRemaining());
    }, 60000); // Update every minute

    return () => clearInterval(interval);
  }, [appointment]);

  const handleSaveFields = useCallback(async () => {
    if (!appointmentIdParam) return;

    try {
      await appointmentService.updateAppointment(Number(appointmentIdParam), {
        complaints,
        anamnesis,
        objectiveData,
        recommendations,
      });
    } catch (err: any) {
      console.error('Ошибка сохранения:', err);
    }
  }, [appointmentIdParam, complaints, anamnesis, objectiveData, recommendations]);

  // Auto-save on field change (debounced)
  useEffect(() => {
    const timer = setTimeout(() => {
      if (appointment && (complaints || anamnesis || objectiveData || recommendations)) {
        handleSaveFields();
      }
    }, 2000);

    return () => clearTimeout(timer);
  }, [complaints, anamnesis, objectiveData, recommendations, appointment, handleSaveFields]);

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

  // Search medications
  const searchMedications = async (query: string) => {
    if (query.length < 2) {
      setMedicationResults([]);
      return;
    }

    try {
      setSearchingMedication(true);
      const results = await prescriptionService.searchMedications(query);
      setMedicationResults(results);
    } catch (err) {
      console.error('Ошибка поиска лекарств:', err);
    } finally {
      setSearchingMedication(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      searchMedications(medicationSearch);
    }, 300);

    return () => clearTimeout(timer);
  }, [medicationSearch]);

  // Search procedures
  const searchProcedures = async (query: string) => {
    if (query.length < 2) {
      setProcedureResults([]);
      return;
    }

    try {
      setSearchingProcedure(true);
      const results = await prescriptionService.searchProcedures(query);
      setProcedureResults(results);
    } catch (err) {
      console.error('Ошибка поиска процедур:', err);
    } finally {
      setSearchingProcedure(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      searchProcedures(procedureSearch);
    }, 300);

    return () => clearTimeout(timer);
  }, [procedureSearch]);

  // Search analyses
  const searchAnalyses = async (query: string) => {
    if (query.length < 2) {
      setAnalysisResults([]);
      return;
    }

    try {
      setSearchingAnalysis(true);
      const results = await prescriptionService.searchAnalyses(query);
      setAnalysisResults(results);
    } catch (err) {
      console.error('Ошибка поиска анализов:', err);
    } finally {
      setSearchingAnalysis(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      searchAnalyses(analysisSearch);
    }, 300);

    return () => clearTimeout(timer);
  }, [analysisSearch]);

  const handleAddDiagnosis = async (diagnosis: Diagnosis) => {
    if (!appointmentIdParam) return;

    try {
      await diagnosisService.addDiagnosisToAppointment({
        appointmentId: Number(appointmentIdParam),
        diagnosisId: diagnosis.idDiagnosis,
        isPrimary: appointment?.diagnoses.length === 0,
      });
      await loadAppointment(Number(appointmentIdParam));
      setDiagnosisSearch('');
      setDiagnosisResults([]);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка добавления диагноза');
    }
  };

  const handleRemoveDiagnosis = async (diagnosisId: number) => {
    if (!appointmentIdParam) return;

    if (!window.confirm('Удалить этот диагноз?')) {
      return;
    }

    try {
      await diagnosisService.removeDiagnosisFromAppointment(diagnosisId);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка удаления диагноза');
    }
  };

  const handleSetPrimaryDiagnosis = async (diagnosisId: number) => {
    if (!appointmentIdParam) return;

    try {
      await diagnosisService.setPrimaryDiagnosis(diagnosisId);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка установки основного диагноза');
    }
  };

  const handleSelectMedication = (medication: Medication) => {
    setSelectedMedication(medication);
    setShowMedicationForm(true);
    setMedicationSearch('');
    setMedicationResults([]);
  };

  const handleAddMedication = async () => {
    if (!appointmentIdParam || !selectedMedication) return;

    if (!medicationDosage || !medicationFrequency || !medicationDuration) {
      alert('Заполните все обязательные поля');
      return;
    }

    try {
      await prescriptionService.createMedicationPrescription({
        appointmentId: Number(appointmentIdParam),
        medicationId: selectedMedication.idMedication,
        dosage: medicationDosage,
        frequency: medicationFrequency,
        duration: Number(medicationDuration),
        instructions: medicationInstructions || undefined,
      });
      await loadAppointment(Number(appointmentIdParam));
      setShowMedicationForm(false);
      setSelectedMedication(null);
      setMedicationDosage('');
      setMedicationFrequency('');
      setMedicationDuration('');
      setMedicationInstructions('');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка добавления лекарства');
    }
  };

  const handleRemoveMedication = async (medicationId: number) => {
    if (!appointmentIdParam) return;

    if (!window.confirm('Удалить это лекарство?')) {
      return;
    }

    try {
      await prescriptionService.deleteMedicationPrescription(medicationId);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка удаления лекарства');
    }
  };

  const handleSelectProcedure = (procedure: Procedure) => {
    setSelectedProcedure(procedure);
    setShowProcedureForm(true);
    setProcedureSearch('');
    setProcedureResults([]);
  };

  const handleAddProcedure = async () => {
    if (!appointmentIdParam || !selectedProcedure) return;

    try {
      await prescriptionService.createProcedurePrescription({
        appointmentId: Number(appointmentIdParam),
        procedureId: selectedProcedure.idProcedure,
        instructions: procedureInstructions || undefined,
      });
      await loadAppointment(Number(appointmentIdParam));
      setShowProcedureForm(false);
      setSelectedProcedure(null);
      setProcedureInstructions('');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка добавления процедуры');
    }
  };

  const handleRemoveProcedure = async (procedureId: number) => {
    if (!appointmentIdParam) return;

    if (!window.confirm('Удалить эту процедуру?')) {
      return;
    }

    try {
      await prescriptionService.deleteProcedurePrescription(procedureId);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка удаления процедуры');
    }
  };

  const handleSelectAnalysis = (analysis: Analysis) => {
    setSelectedAnalysis(analysis);
    setShowAnalysisForm(true);
    setAnalysisSearch('');
    setAnalysisResults([]);
  };

  const handleAddAnalysis = async () => {
    if (!appointmentIdParam || !selectedAnalysis) return;

    try {
      await prescriptionService.createAnalysisPrescription({
        appointmentId: Number(appointmentIdParam),
        analysisId: selectedAnalysis.idAnalysis,
        instructions: analysisInstructions || undefined,
      });
      await loadAppointment(Number(appointmentIdParam));
      setShowAnalysisForm(false);
      setSelectedAnalysis(null);
      setAnalysisInstructions('');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка добавления анализа');
    }
  };

  const handleRemoveAnalysis = async (analysisId: number) => {
    if (!appointmentIdParam) return;

    if (!window.confirm('Удалить этот анализ?')) {
      return;
    }

    try {
      await prescriptionService.deleteAnalysisPrescription(analysisId);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка удаления анализа');
    }
  };

  const handleDeleteFile = async (fileId: number) => {
    if (!appointmentIdParam) return;

    if (!window.confirm('Удалить этот файл?')) {
      return;
    }

    try {
      await fileService.deleteFile(fileId);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка удаления файла');
    }
  };

  const handleApplyTemplate = async (template: TemplateResponse) => {
    setComplaints(template.complaints || '');
    setAnamnesis(template.anamnesis || '');
    setObjectiveData(template.examination || '');
    setRecommendations(template.recommendations || '');
    setShowTemplateModal(false);

    // Save the fields immediately
    if (appointmentIdParam) {
      try {
        await appointmentService.updateAppointment(Number(appointmentIdParam), {
          complaints: template.complaints || '',
          anamnesis: template.anamnesis || '',
          objectiveData: template.examination || '',
          recommendations: template.recommendations || '',
        });
      } catch (err) {
        console.error('Ошибка сохранения полей шаблона:', err);
      }
    }

    // Add diagnoses from template
    if (template.diagnoses && appointmentIdParam && appointment) {
      for (const diagnosis of template.diagnoses) {
        // Check if diagnosis already exists
        const alreadyExists = appointment.diagnoses.some(
          d => d.diagnosisId === diagnosis.idDiagnosis
        );

        if (alreadyExists) {
          continue; // Skip if already added
        }

        try {
          await diagnosisService.addDiagnosisToAppointment({
            appointmentId: Number(appointmentIdParam),
            diagnosisId: diagnosis.idDiagnosis,
            isPrimary: diagnosis.isPrimary || false,
          });
        } catch (err) {
          // Silently ignore errors (e.g., duplicate diagnosis)
          console.error('Ошибка добавления диагноза из шаблона:', err);
        }
      }
      await loadAppointment(Number(appointmentIdParam));
    }
  };

  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file || !appointmentIdParam) return;

    try {
      setUploadingFile(true);
      await fileService.uploadFile(Number(appointmentIdParam), file);
      await loadAppointment(Number(appointmentIdParam));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка загрузки файла');
    } finally {
      setUploadingFile(false);
    }
  };

  const handleDownloadFile = async (fileId: number, fileName: string) => {
    try {
      const blob = await fileService.downloadFile(fileId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка скачивания файла');
    }
  };

  const handleFinishAppointment = async () => {
    if (!appointment || !appointmentIdParam) return;

    // Check if cancelled
    if (appointment.status === 'cancelled') {
      alert('Невозможно завершить отмененный прием');
      return;
    }

    // Check if already completed
    if (appointment.status === 'completed') {
      alert('Этот прием уже завершен');
      return;
    }

    if (!window.confirm('Завершить прием? Все данные будут сохранены.')) {
      return;
    }

    try {
      await handleSaveFields();
      await appointmentService.completeAppointment(Number(appointmentIdParam));

      // Get next appointment
      const today = new Date().toISOString().split('T')[0];
      const todayAppointments = await appointmentService.getDoctorAppointmentsByDate(today);

      const currentIndex = todayAppointments.findIndex(
        (apt) => apt.idAppointment === Number(appointmentIdParam)
      );

      if (currentIndex !== -1 && currentIndex < todayAppointments.length - 1) {
        const nextAppointment = todayAppointments[currentIndex + 1];
        navigate(`/doctor/patients/${nextAppointment.patient.id}?appointmentId=${nextAppointment.idAppointment}&tab=examination`);
      } else {
        navigate('/doctor/dashboard');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Ошибка завершения приема');
    }
  };

  const calculateAge = (birthdate: string) => {
    const birth = new Date(birthdate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  };

  const canCompleteAppointment = () => {
    if (!appointment) return false;
    if (appointment.status === 'completed') return false;
    if (appointment.status === 'cancelled') return false;

    // Check if appointment time has started and not ended yet
    const now = new Date();
    const aptDate = appointment.slotDate;
    const aptStartTimeStr = appointment.startTime;
    const aptEndTimeStr = appointment.endTime;

    if (aptDate && aptStartTimeStr && aptEndTimeStr) {
      const aptStartTime = new Date(`${aptDate}T${aptStartTimeStr}`);
      const aptEndTime = new Date(`${aptDate}T${aptEndTimeStr}`);

      // Allow editing only if appointment has started
      if (now < aptStartTime) {
        return false;
      }

      // Allow completion only if appointment hasn't ended yet or within 5 minutes after end
      const fiveMinutesAfterEnd = new Date(aptEndTime.getTime() + 5 * 60 * 1000);
      if (now > fiveMinutesAfterEnd) {
        return false;
      }
    }

    return true;
  };

  if (loading) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center' }}>Загрузка...</div>
      </Layout>
    );
  }

  if (error && !appointment) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center', color: 'red' }}>
          {error}
        </div>
      </Layout>
    );
  }

  if (!appointment && patientHistory.length === 0) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center' }}>
          Данные не найдены
        </div>
      </Layout>
    );
  }

  // If no appointment but have history, show patient info from first history item
  const displayPatient = appointment?.patient || patientHistory[0]?.patient;
  if (!displayPatient) {
    return (
      <Layout>
        <div style={{ padding: '40px', textAlign: 'center' }}>
          Пациент не найден
        </div>
      </Layout>
    );
  }

  const fullName = `${displayPatient.lastName} ${displayPatient.firstName} ${displayPatient.middleName || ''}`.trim();

  const formatAppointmentDateTime = () => {
    if (!appointment) return null;
    const date = new Date(appointment.slotDate);
    const dateStr = date.toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
    return `${dateStr}, ${appointment.startTime} - ${appointment.endTime}`;
  };

  return (
    <Layout>
      <div className="appointment-page">
        <div className="appointment-header">
          <div className="patient-info">
            <div className="patient-details">
              <h1>{fullName}</h1>
              {appointment && (
                <div style={{ color: '#718096', fontSize: '14px', marginBottom: '8px' }}>
                  Прием: {formatAppointmentDateTime()}
                </div>
              )}
              <div className="patient-meta">
                <span>Возраст: {calculateAge(displayPatient.dateOfBirth)} лет</span>
                <span>Дата рождения: {displayPatient.dateOfBirth}</span>
                <span>СНИЛС: {displayPatient.snils}</span>
              </div>
            </div>
            <div className="appointment-actions">
              {appointment && canCompleteAppointment() && timeRemaining > 0 && (
                <div className="timer">⏱️ Осталось {timeRemaining} минут</div>
              )}
              {appointment && canCompleteAppointment() && (
                <button className="btn btn-primary" onClick={handleFinishAppointment}>
                  Завершить прием
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="tabs">
          <div className="tabs-header">
            <button
              className={`tab-button ${activeTab === 'history' ? 'active' : ''}`}
              onClick={() => setActiveTab('history')}
            >
              История
            </button>
            <button
              className={`tab-button ${activeTab === 'examination' ? 'active' : ''}`}
              onClick={() => setActiveTab('examination')}
            >
              Осмотр
            </button>
          </div>

          <div className="tab-content">
            {activeTab === 'history' && (
              <div className="history-list">
                {loadingHistory ? (
                  <div style={{ textAlign: 'center', padding: '40px' }}>Загрузка истории...</div>
                ) : patientHistory.length > 0 ? (
                  patientHistory.map((historyItem: any) => (
                    <div key={historyItem.id} className="history-item">
                      <div className="history-header">
                        <span className="history-date">
                          {(() => {
                            const parts = historyItem.date.split(/[-T:]/);
                            const date = new Date(
                              parseInt(parts[0]),
                              parseInt(parts[1]) - 1,
                              parseInt(parts[2]),
                              parseInt(parts[3] || '0'),
                              parseInt(parts[4] || '0'),
                              parseInt(parts[5] || '0')
                            );
                            return date.toLocaleDateString('ru-RU') + ' ' + date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
                          })()}
                        </span>
                        {historyItem.doctor && (
                          <span className="history-doctor">
                            Врач: {historyItem.doctor.lastName} {historyItem.doctor.firstName}
                          </span>
                        )}
                      </div>
                      {historyItem.diagnosis && (
                        <div className="history-diagnosis">
                          Диагноз: {historyItem.diagnosis}
                        </div>
                      )}
                      {historyItem.prescriptions && (
                        <div className="history-prescriptions">
                          Назначения: {historyItem.prescriptions}
                        </div>
                      )}
                      {historyItem.notes && (
                        <div className="history-prescriptions">
                          Рекомендации: {historyItem.notes}
                        </div>
                      )}
                      <button
                        className="btn-link"
                        onClick={() => navigate(`/doctor/patients/${displayPatient.id}?appointmentId=${historyItem.appointmentId}&tab=examination`)}
                      >
                        Подробнее
                      </button>
                    </div>
                  ))
                ) : (
                  <p>История приемов пока пуста</p>
                )}
              </div>
            )}

            {activeTab === 'examination' && !appointment && (
              <div style={{ padding: '40px', textAlign: 'center' }}>
                <p>Выберите прием из истории или расписания для просмотра данных осмотра</p>
              </div>
            )}

            {activeTab === 'examination' && appointment && (
              <div className="examination-form">
                {appointment.status === 'cancelled' && (
                  <div style={{
                    padding: '20px',
                    background: '#fee',
                    border: '1px solid #fcc',
                    borderRadius: '8px',
                    marginBottom: '20px',
                    color: '#c00'
                  }}>
                    <strong>⚠️ Этот прием был отменен</strong>
                    <p style={{ margin: '8px 0 0 0' }}>Невозможно редактировать или завершить отмененный прием.</p>
                  </div>
                )}
                {canCompleteAppointment() && (
                  <div className="form-actions" style={{ justifyContent: 'flex-start', paddingTop: 0, borderTop: 'none' }}>
                    <button className="templates-button" onClick={() => setShowTemplateModal(true)}>
                      📝 Использовать шаблон
                    </button>
                  </div>
                )}

                {appointment.status === 'completed' && (
                  <div className="form-actions" style={{ justifyContent: 'flex-start', paddingTop: 0, borderTop: 'none' }}>
                    <button
                      className="btn btn-secondary"
                      onClick={() => setShowCertificateModal(true)}
                    >
                      📄 Создать справку
                    </button>
                  </div>
                )}

                {appointment.status === 'completed' && certificates.length > 0 && (
                  <div className="form-section">
                    <h3>Справки</h3>
                    <div className="certificates-list">
                      {certificates.map((cert) => (
                        <div key={cert.idCertificate} className="certificate-item" style={{
                          padding: '12px',
                          border: '1px solid #e2e8f0',
                          borderRadius: '8px',
                          marginBottom: '8px',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center'
                        }}>
                          <div>
                            <strong>{cert.certificateTypeName}</strong>
                            <div style={{ fontSize: '12px', color: '#718096', marginTop: '4px' }}>
                              Создана: {new Date(cert.createdAt).toLocaleDateString('ru-RU')}
                            </div>
                            {cert.validFrom && cert.validTo && (
                              <div style={{ fontSize: '12px', color: '#718096' }}>
                                Действительна: {new Date(cert.validFrom).toLocaleDateString('ru-RU')} - {new Date(cert.validTo).toLocaleDateString('ru-RU')}
                              </div>
                            )}
                          </div>
                          <button
                            className="btn btn-secondary"
                            onClick={() => handleDownloadCertificate(cert.idCertificate, cert.certificateTypeName)}
                          >
                            📥 Скачать
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <div className="form-section">
                  <h3>Жалобы</h3>
                  <div className="form-group">
                    <textarea
                      placeholder="Опишите жалобы пациента..."
                      value={complaints}
                      onChange={(e) => setComplaints(e.target.value)}
                      disabled={!canCompleteAppointment()}
                    />
                  </div>
                </div>

                <div className="form-section">
                  <h3>Анамнез заболевания</h3>
                  <div className="form-group">
                    <textarea
                      placeholder="История развития заболевания..."
                      value={anamnesis}
                      onChange={(e) => setAnamnesis(e.target.value)}
                      disabled={!canCompleteAppointment()}
                    />
                  </div>
                </div>

                <div className="form-section">
                  <h3>Объективные данные</h3>
                  <div className="form-group">
                    <textarea
                      placeholder="АД, пульс, температура, результаты осмотра..."
                      value={objectiveData}
                      onChange={(e) => setObjectiveData(e.target.value)}
                      disabled={!canCompleteAppointment()}
                    />
                  </div>
                </div>

                <div className="form-section">
                  <h3>Диагноз</h3>
                  <div className="form-group">
                    {canCompleteAppointment() && (
                      <>
                        <label>Поиск по МКБ-11</label>
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
                      </>
                    )}
                    {appointment.diagnoses.length > 0 && (
                      <div className="selected-diagnoses">
                        {appointment.diagnoses.map((diagnosis) => (
                          <div key={diagnosis.id} className="diagnosis-tag">
                            <div style={{ flex: 1 }}>
                              <strong>{diagnosis.icdCode}</strong> - {diagnosis.diagnosisName}
                              {diagnosis.isPrimary && <span className="primary-badge">Основной</span>}
                            </div>
                            {canCompleteAppointment() && (
                              <div style={{ display: 'flex', gap: '8px', marginLeft: '12px' }}>
                                {!diagnosis.isPrimary && (
                                  <button
                                    onClick={() => handleSetPrimaryDiagnosis(diagnosis.id)}
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
                                  onClick={() => handleRemoveDiagnosis(diagnosis.id)}
                                  style={{
                                    padding: '4px 8px',
                                    fontSize: '12px',
                                    background: '#ef4444',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    cursor: 'pointer'
                                  }}
                                  title="Удалить"
                                >
                                  ✕
                                </button>
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                <div className="form-section">
                  <h3>Назначения - Лекарства</h3>
                  {canCompleteAppointment() && !showMedicationForm && (
                    <>
                      <div className="form-group">
                        <label>Поиск лекарства</label>
                        <div className="diagnosis-search">
                          <input
                            type="text"
                            placeholder="Введите название лекарства..."
                            value={medicationSearch}
                            onChange={(e) => setMedicationSearch(e.target.value)}
                          />
                          {searchingMedication && <div className="search-loading">Поиск...</div>}
                          {medicationResults.length > 0 && (
                            <div className="diagnosis-results">
                              {medicationResults.map((medication) => (
                                <div
                                  key={medication.idMedication}
                                  className="diagnosis-result-item"
                                  onClick={() => handleSelectMedication(medication)}
                                >
                                  <strong>{medication.title}</strong>
                                  {medication.form && <span> - {medication.form}</span>}
                                  {medication.activeSubstance && <div style={{ fontSize: '12px', color: '#718096' }}>{medication.activeSubstance}</div>}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </>
                  )}
                  {showMedicationForm && selectedMedication && (
                    <div className="prescription-form">
                      <div style={{ marginBottom: '15px', padding: '10px', background: '#f7fafc', borderRadius: '4px' }}>
                        <strong>{selectedMedication.title}</strong>
                        {selectedMedication.form && <span> - {selectedMedication.form}</span>}
                      </div>
                      <div className="form-group">
                        <label>Дозировка *</label>
                        <input
                          type="text"
                          placeholder="Например: 500 мг"
                          value={medicationDosage}
                          onChange={(e) => setMedicationDosage(e.target.value)}
                        />
                      </div>
                      <div className="form-group">
                        <label>Частота приема *</label>
                        <input
                          type="text"
                          placeholder="Например: 2 раза в день"
                          value={medicationFrequency}
                          onChange={(e) => setMedicationFrequency(e.target.value)}
                        />
                      </div>
                      <div className="form-group">
                        <label>Длительность (дней) *</label>
                        <input
                          type="number"
                          placeholder="Например: 7"
                          value={medicationDuration}
                          onChange={(e) => setMedicationDuration(e.target.value)}
                        />
                      </div>
                      <div className="form-group">
                        <label>Инструкции</label>
                        <textarea
                          placeholder="Дополнительные указания..."
                          value={medicationInstructions}
                          onChange={(e) => setMedicationInstructions(e.target.value)}
                          rows={2}
                        />
                      </div>
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button className="btn btn-primary" onClick={handleAddMedication}>
                          Добавить
                        </button>
                        <button className="btn btn-secondary" onClick={() => {
                          setShowMedicationForm(false);
                          setSelectedMedication(null);
                          setMedicationDosage('');
                          setMedicationFrequency('');
                          setMedicationDuration('');
                          setMedicationInstructions('');
                        }}>
                          Отмена
                        </button>
                      </div>
                    </div>
                  )}
                  {appointment.medications.length > 0 && (
                    <div className="prescriptions-list" style={{ marginTop: '15px' }}>
                      {appointment.medications.map((med) => (
                        <div key={med.id} className="prescription-item-display" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <div style={{ flex: 1 }}>
                            <strong>{med.medicationName}</strong>
                            {med.dosage && <span> - {med.dosage}</span>}
                            {med.frequency && <span> - {med.frequency}</span>}
                            {med.duration && <span> - {med.duration} дней</span>}
                            {med.instructions && <div className="instructions">{med.instructions}</div>}
                          </div>
                          {canCompleteAppointment() && (
                            <button
                              onClick={() => handleRemoveMedication(med.id)}
                              style={{
                                padding: '4px 8px',
                                fontSize: '12px',
                                background: '#ef4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                marginLeft: '12px'
                              }}
                              title="Удалить"
                            >
                              ✕
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                  {appointment.medications.length === 0 && !showMedicationForm && !canCompleteAppointment() && (
                    <p>Назначений пока нет</p>
                  )}
                </div>

                <div className="form-section">
                  <h3>Процедуры</h3>
                  {canCompleteAppointment() && !showProcedureForm && (
                    <>
                      <div className="form-group">
                        <label>Поиск процедуры</label>
                        <div className="diagnosis-search">
                          <input
                            type="text"
                            placeholder="Введите название процедуры..."
                            value={procedureSearch}
                            onChange={(e) => setProcedureSearch(e.target.value)}
                          />
                          {searchingProcedure && <div className="search-loading">Поиск...</div>}
                          {procedureResults.length > 0 && (
                            <div className="diagnosis-results">
                              {procedureResults.map((procedure) => (
                                <div
                                  key={procedure.idProcedure}
                                  className="diagnosis-result-item"
                                  onClick={() => handleSelectProcedure(procedure)}
                                >
                                  <strong>{procedure.title}</strong>
                                  {procedure.description && <div style={{ fontSize: '12px', color: '#718096' }}>{procedure.description}</div>}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </>
                  )}
                  {showProcedureForm && selectedProcedure && (
                    <div className="prescription-form">
                      <div style={{ marginBottom: '15px', padding: '10px', background: '#f7fafc', borderRadius: '4px' }}>
                        <strong>{selectedProcedure.title}</strong>
                        {selectedProcedure.description && <div style={{ fontSize: '12px', color: '#718096', marginTop: '4px' }}>{selectedProcedure.description}</div>}
                      </div>
                      <div className="form-group">
                        <label>Инструкции</label>
                        <textarea
                          placeholder="Дополнительные указания..."
                          value={procedureInstructions}
                          onChange={(e) => setProcedureInstructions(e.target.value)}
                          rows={2}
                        />
                      </div>
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button className="btn btn-primary" onClick={handleAddProcedure}>
                          Добавить
                        </button>
                        <button className="btn btn-secondary" onClick={() => {
                          setShowProcedureForm(false);
                          setSelectedProcedure(null);
                          setProcedureInstructions('');
                        }}>
                          Отмена
                        </button>
                      </div>
                    </div>
                  )}
                  {appointment.procedures.length > 0 && (
                    <div className="prescriptions-list" style={{ marginTop: '15px' }}>
                      {appointment.procedures.map((proc) => (
                        <div key={proc.id} className="prescription-item-display" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <div style={{ flex: 1 }}>
                            <strong>{proc.procedureName}</strong>
                            {proc.instructions && <div className="instructions">{proc.instructions}</div>}
                          </div>
                          {canCompleteAppointment() && (
                            <button
                              onClick={() => handleRemoveProcedure(proc.id)}
                              style={{
                                padding: '4px 8px',
                                fontSize: '12px',
                                background: '#ef4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                marginLeft: '12px'
                              }}
                              title="Удалить"
                            >
                              ✕
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                  {appointment.procedures.length === 0 && !showProcedureForm && !canCompleteAppointment() && (
                    <p>Процедур пока нет</p>
                  )}
                </div>

                <div className="form-section">
                  <h3>Анализы</h3>
                  {canCompleteAppointment() && !showAnalysisForm && (
                    <>
                      <div className="form-group">
                        <label>Поиск анализа</label>
                        <div className="diagnosis-search">
                          <input
                            type="text"
                            placeholder="Введите название или код анализа..."
                            value={analysisSearch}
                            onChange={(e) => setAnalysisSearch(e.target.value)}
                          />
                          {searchingAnalysis && <div className="search-loading">Поиск...</div>}
                          {analysisResults.length > 0 && (
                            <div className="diagnosis-results">
                              {analysisResults.map((analysis) => (
                                <div
                                  key={analysis.idAnalysis}
                                  className="diagnosis-result-item"
                                  onClick={() => handleSelectAnalysis(analysis)}
                                >
                                  <strong>{analysis.code}</strong> - {analysis.title}
                                  {analysis.description && <div style={{ fontSize: '12px', color: '#718096' }}>{analysis.description}</div>}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </>
                  )}
                  {showAnalysisForm && selectedAnalysis && (
                    <div className="prescription-form">
                      <div style={{ marginBottom: '15px', padding: '10px', background: '#f7fafc', borderRadius: '4px' }}>
                        <strong>{selectedAnalysis.code}</strong> - {selectedAnalysis.title}
                        {selectedAnalysis.description && <div style={{ fontSize: '12px', color: '#718096', marginTop: '4px' }}>{selectedAnalysis.description}</div>}
                      </div>
                      <div className="form-group">
                        <label>Инструкции</label>
                        <textarea
                          placeholder="Дополнительные указания..."
                          value={analysisInstructions}
                          onChange={(e) => setAnalysisInstructions(e.target.value)}
                          rows={2}
                        />
                      </div>
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button className="btn btn-primary" onClick={handleAddAnalysis}>
                          Добавить
                        </button>
                        <button className="btn btn-secondary" onClick={() => {
                          setShowAnalysisForm(false);
                          setSelectedAnalysis(null);
                          setAnalysisInstructions('');
                        }}>
                          Отмена
                        </button>
                      </div>
                    </div>
                  )}
                  {appointment.analyses.length > 0 && (
                    <div className="prescriptions-list" style={{ marginTop: '15px' }}>
                      {appointment.analyses.map((analysis) => (
                        <div key={analysis.id} className="prescription-item-display" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <div style={{ flex: 1 }}>
                            <strong>{analysis.analysisName}</strong>
                            {analysis.instructions && <div className="instructions">{analysis.instructions}</div>}
                          </div>
                          {canCompleteAppointment() && (
                            <button
                              onClick={() => handleRemoveAnalysis(analysis.id)}
                              style={{
                                padding: '4px 8px',
                                fontSize: '12px',
                                background: '#ef4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                marginLeft: '12px'
                              }}
                              title="Удалить"
                            >
                              ✕
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                  {appointment.analyses.length === 0 && !showAnalysisForm && !canCompleteAppointment() && (
                    <p>Анализов пока нет</p>
                  )}
                </div>

                <div className="form-section">
                  <h3>Рекомендации</h3>
                  <div className="form-group">
                    <textarea
                      placeholder="Рекомендации для пациента..."
                      value={recommendations}
                      onChange={(e) => setRecommendations(e.target.value)}
                      disabled={!canCompleteAppointment()}
                    />
                  </div>
                </div>

                <div className="form-section">
                  <h3>Прикрепленные файлы</h3>
                  {appointment.files.length > 0 && (
                    <div className="files-list">
                      {appointment.files.map((file) => (
                        <div key={file.id} className="file-item" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0' }}>
                          <button
                            onClick={() => handleDownloadFile(file.id, file.fileName)}
                            style={{
                              background: 'none',
                              border: 'none',
                              color: '#2563eb',
                              cursor: 'pointer',
                              textDecoration: 'underline',
                              padding: 0,
                              font: 'inherit'
                            }}
                          >
                            📎 {file.fileName} ({file.fileSize ? (file.fileSize / 1024).toFixed(2) + ' KB' : 'N/A'})
                          </button>
                          {canCompleteAppointment() && (
                            <button
                              onClick={() => handleDeleteFile(file.id)}
                              style={{
                                padding: '4px 8px',
                                fontSize: '12px',
                                background: '#ef4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                marginLeft: '12px'
                              }}
                              title="Удалить"
                            >
                              ✕
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                  {canCompleteAppointment() && (
                    <div className="file-upload">
                      <input
                        type="file"
                        id="file-upload"
                        style={{ display: 'none' }}
                        onChange={handleFileUpload}
                        disabled={uploadingFile}
                      />
                      <label htmlFor="file-upload" className="btn btn-secondary">
                        {uploadingFile ? 'Загрузка...' : '📎 Прикрепить файл'}
                      </label>
                    </div>
                  )}
                </div>

                {canCompleteAppointment() && (
                  <div className="form-actions">
                    <button className="btn btn-primary" onClick={handleFinishAppointment}>
                      Завершить прием
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Template Modal */}
      {showTemplateModal && (
        <div className="modal-overlay" onClick={() => setShowTemplateModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Выберите шаблон</h2>
              <button className="modal-close" onClick={() => setShowTemplateModal(false)}>
                ✕
              </button>
            </div>
            <div className="modal-body">
              {templates.length > 0 ? (
                <div className="templates-list">
                  {templates.map((template) => (
                    <div
                      key={template.idTemplate}
                      className="template-item"
                      onClick={() => handleApplyTemplate(template)}
                    >
                      <h3>{template.title}</h3>
                      {template.complaints && <p className="template-preview">{template.complaints.substring(0, 100)}...</p>}
                    </div>
                  ))}
                </div>
              ) : (
                <p>У вас пока нет сохраненных шаблонов</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Certificate Modal */}
      {showCertificateModal && (
        <div className="modal-overlay" onClick={() => setShowCertificateModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Создать справку</h2>
              <button className="modal-close" onClick={() => setShowCertificateModal(false)}>
                ✕
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>Тип справки</label>
                <select
                  value={certificateType}
                  onChange={(e) => setCertificateType(e.target.value as 'visit' | 'work_study')}
                  style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                >
                  <option value="visit">Справка о посещении врача</option>
                  <option value="work_study">Справка для работы/учебы (095/у)</option>
                </select>
              </div>

              <div className="form-group">
                <label>Срок действия справки (необязательно)</label>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                  <input
                    type="date"
                    value={validFrom}
                    onChange={(e) => setValidFrom(e.target.value)}
                    style={{ flex: 1, padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                    placeholder="С"
                  />
                  <span>—</span>
                  <input
                    type="date"
                    value={validTo}
                    onChange={(e) => setValidTo(e.target.value)}
                    style={{ flex: 1, padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                    placeholder="По"
                  />
                </div>
              </div>

              {certificateType === 'work_study' && (
                <>
                  <div className="form-group">
                    <label>Период нетрудоспособности *</label>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                      <input
                        type="date"
                        value={disabilityPeriodFrom}
                        onChange={(e) => setDisabilityPeriodFrom(e.target.value)}
                        style={{ flex: 1, padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                        placeholder="С"
                        required
                      />
                      <span>—</span>
                      <input
                        type="date"
                        value={disabilityPeriodTo}
                        onChange={(e) => setDisabilityPeriodTo(e.target.value)}
                        style={{ flex: 1, padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                        placeholder="По"
                        required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Рекомендации по режиму (необязательно)</label>
                    <textarea
                      value={workRestrictions}
                      onChange={(e) => setWorkRestrictions(e.target.value)}
                      placeholder="Например: освобождение от физических нагрузок, легкий труд..."
                      style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0', minHeight: '80px' }}
                    />
                  </div>
                </>
              )}

              <div className="form-actions" style={{ marginTop: '20px' }}>
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowCertificateModal(false)}
                  disabled={creatingCertificate}
                >
                  Отмена
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleCreateCertificate}
                  disabled={creatingCertificate}
                >
                  {creatingCertificate ? 'Создание...' : 'Создать справку'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

export default PatientCard;
