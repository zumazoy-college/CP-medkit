package com.medkit.backend.repository;

import com.medkit.backend.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {

    List<Certificate> findByPatient_IdPatientOrderByCreatedAtDesc(Integer patientId);

    List<Certificate> findByAppointment_IdAppointment(Integer appointmentId);

    List<Certificate> findByDoctor_IdDoctorOrderByCreatedAtDesc(Integer doctorId);
}
