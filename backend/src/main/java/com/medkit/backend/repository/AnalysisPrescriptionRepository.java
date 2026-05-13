package com.medkit.backend.repository;

import com.medkit.backend.entity.AnalysisPrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisPrescriptionRepository extends JpaRepository<AnalysisPrescription, Integer> {

    List<AnalysisPrescription> findByAppointment_IdAppointmentOrderByCreatedAtDesc(Integer appointmentId);

    List<AnalysisPrescription> findByAppointment_Patient_IdPatientOrderByCreatedAtDesc(Integer patientId);
}
