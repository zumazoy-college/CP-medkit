import { useState, useEffect } from 'react';
import appointmentService, { AppointmentFilters } from '../api/appointment.service';
import { Appointment } from '../types';

export const useAppointments = (filters?: AppointmentFilters) => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAppointments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getAppointments(filters);
      setAppointments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch appointments');
      console.error('Error fetching appointments:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, [JSON.stringify(filters)]);

  return { appointments, loading, error, refetch: fetchAppointments };
};

export const useAppointment = (id: number) => {
  const [appointment, setAppointment] = useState<Appointment | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAppointment = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getAppointmentById(id);
      setAppointment(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch appointment');
      console.error('Error fetching appointment:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchAppointment();
    }
  }, [id]);

  return { appointment, loading, error, refetch: fetchAppointment };
};

export const useTodayAppointments = (doctorId: number) => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTodayAppointments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await appointmentService.getTodayAppointments(doctorId);
      setAppointments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch today appointments');
      console.error('Error fetching today appointments:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (doctorId && doctorId > 0) {
      fetchTodayAppointments();
    } else {
      setLoading(false);
    }
  }, [doctorId]);

  return { appointments, loading, error, refetch: fetchTodayAppointments };
};
