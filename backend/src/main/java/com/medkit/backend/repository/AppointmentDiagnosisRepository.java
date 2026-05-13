package com.medkit.backend.repository;

import com.medkit.backend.entity.AppointmentDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentDiagnosisRepository extends JpaRepository<AppointmentDiagnosis, Integer> {

    List<AppointmentDiagnosis> findByAppointment_IdAppointment(Integer appointmentId);
}
