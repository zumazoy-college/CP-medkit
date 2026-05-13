import { useState, useEffect } from 'react';
import patientService, { PatientSearchParams } from '../api/patient.service';
import { Patient, MedicalHistory } from '../types';

export const usePatients = (params?: PatientSearchParams) => {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPatients = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await patientService.getPatients(params);
      setPatients(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch patients');
      console.error('Error fetching patients:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPatients();
  }, [JSON.stringify(params)]);

  return { patients, loading, error, refetch: fetchPatients };
};

export const usePatient = (id: number | null) => {
  const [patient, setPatient] = useState<Patient | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPatient = async () => {
    if (!id) {
      setPatient(null);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await patientService.getPatientById(id);
      setPatient(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch patient');
      console.error('Error fetching patient:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPatient();
  }, [id]);

  return { patient, loading, error, refetch: fetchPatient };
};

export const usePatientHistory = (patientId: number | null) => {
  const [history, setHistory] = useState<MedicalHistory[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchHistory = async () => {
    if (!patientId) {
      setHistory([]);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await patientService.getPatientHistory(patientId);
      setHistory(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch patient history');
      console.error('Error fetching patient history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [patientId]);

  return { history, loading, error, refetch: fetchHistory };
};

export const usePatientSearch = () => {
  const [results, setResults] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const search = async (query: string) => {
    if (!query.trim()) {
      setResults([]);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await patientService.searchPatients(query);
      setResults(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to search patients');
      console.error('Error searching patients:', err);
    } finally {
      setLoading(false);
    }
  };

  return { results, loading, error, search };
};
