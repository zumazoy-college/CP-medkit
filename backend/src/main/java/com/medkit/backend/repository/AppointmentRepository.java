package com.medkit.backend.repository;

import com.medkit.backend.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    Page<Appointment> findByPatient_IdPatientOrderByCreatedAtDesc(Integer patientId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.slot.doctor.idDoctor = :doctorId " +
           "ORDER BY a.createdAt DESC")
    Page<Appointment> findByDoctorId(@Param("doctorId") Integer doctorId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.slot.doctor.idDoctor = :doctorId " +
           "AND a.slot.slotDate = :date")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Integer doctorId,
                                             @Param("date") LocalDate date);

    List<Appointment> findByPatient_IdPatientAndSlot_Status_TitleOrderByCreatedAtDesc(
            Integer patientId, String status);

    Optional<Appointment> findBySlot_IdSlot(Integer slotId);

    @Query("DELETE FROM Appointment a WHERE a.slot.idSlot = :slotId")
    @Modifying
    void deleteBySlotId(@Param("slotId") Integer slotId);
}
