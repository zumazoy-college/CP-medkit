package com.medkit.backend.repository;

import com.medkit.backend.entity.MedicationPrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationPrescriptionRepository extends JpaRepository<MedicationPrescription, Integer> {

    List<MedicationPrescription> findByAppointment_IdAppointmentOrderByCreatedAtDesc(Integer appointmentId);

    List<MedicationPrescription> findByAppointment_Patient_IdPatientOrderByCreatedAtDesc(Integer patientId);
}
