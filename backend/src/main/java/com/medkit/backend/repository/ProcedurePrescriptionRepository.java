package com.medkit.backend.repository;

import com.medkit.backend.entity.ProcedurePrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedurePrescriptionRepository extends JpaRepository<ProcedurePrescription, Integer> {

    List<ProcedurePrescription> findByAppointment_IdAppointmentOrderByCreatedAtDesc(Integer appointmentId);

    List<ProcedurePrescription> findByAppointment_Patient_IdPatientOrderByCreatedAtDesc(Integer patientId);
}
