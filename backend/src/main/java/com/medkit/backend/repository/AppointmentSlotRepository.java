package com.medkit.backend.repository;

import com.medkit.backend.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Integer> {

    List<AppointmentSlot> findByDoctor_IdDoctorAndSlotDate(Integer doctorId, LocalDate slotDate);

    @Query("SELECT s FROM AppointmentSlot s WHERE s.doctor.idDoctor = :doctorId " +
           "AND s.slotDate = :slotDate AND s.status.title = 'free' " +
           "ORDER BY s.startTime ASC")
    List<AppointmentSlot> findAvailableSlots(@Param("doctorId") Integer doctorId,
                                              @Param("slotDate") LocalDate slotDate);

    List<AppointmentSlot> findByPatient_IdPatientOrderBySlotDateDesc(Integer patientId);

    Optional<AppointmentSlot> findByDoctor_IdDoctorAndSlotDateAndStartTime(
        Integer doctorId, LocalDate slotDate, LocalTime startTime);

    @Query("SELECT s FROM AppointmentSlot s WHERE s.doctor.idDoctor = :doctorId " +
           "AND s.slotDate BETWEEN :startDate AND :endDate")
    List<AppointmentSlot> findByDoctorAndDateRange(@Param("doctorId") Integer doctorId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    List<AppointmentSlot> findByDoctor_IdDoctorAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
        Integer doctorId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM AppointmentSlot s WHERE s.doctor.idDoctor = :doctorId " +
           "AND s.slotDate BETWEEN :startDate AND :endDate " +
           "AND s.status.title = 'free' " +
           "ORDER BY s.slotDate ASC, s.startTime ASC")
    List<AppointmentSlot> findAvailableSlotsByDoctorAndDateRange(@Param("doctorId") Integer doctorId,
                                                                   @Param("startDate") LocalDate startDate,
                                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "((s.slotDate = :date AND s.startTime >= :startTime AND s.startTime < :endTime) OR " +
           "(s.slotDate = :nextDate AND s.startTime >= :nextDayStartTime AND s.startTime < :nextDayEndTime)) " +
           "AND s.status.title = 'booked' " +
           "AND s.patient IS NOT NULL")
    List<AppointmentSlot> findUpcomingAppointmentsForReminder(@Param("date") LocalDate date,
                                                                @Param("startTime") LocalTime startTime,
                                                                @Param("endTime") LocalTime endTime,
                                                                @Param("nextDate") LocalDate nextDate,
                                                                @Param("nextDayStartTime") LocalTime nextDayStartTime,
                                                                @Param("nextDayEndTime") LocalTime nextDayEndTime);
}
