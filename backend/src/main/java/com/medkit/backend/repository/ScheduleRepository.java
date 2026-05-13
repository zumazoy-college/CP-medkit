package com.medkit.backend.repository;

import com.medkit.backend.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByDoctor_IdDoctorAndIsActiveTrue(Integer doctorId);

    Optional<Schedule> findByDoctor_IdDoctorAndDayOfWeek(Integer doctorId, Integer dayOfWeek);

    List<Schedule> findByDayOfWeekAndIsActiveTrue(Integer dayOfWeek);

    /**
     * Find active schedule for a specific doctor, day of week, and date
     * The schedule is active if:
     * - is_active = true
     * - effectiveFrom <= date
     * - effectiveTo is null OR effectiveTo >= date
     */
    @Query("SELECT s FROM Schedule s WHERE s.doctor.idDoctor = :doctorId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND s.isActive = true " +
           "AND s.effectiveFrom <= :date " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)")
    Optional<Schedule> findActiveScheduleForDate(@Param("doctorId") Integer doctorId,
                                                   @Param("dayOfWeek") Integer dayOfWeek,
                                                   @Param("date") LocalDate date);

    /**
     * Find all schedules that overlap with a given date range for a specific doctor and day
     */
    @Query("SELECT s FROM Schedule s WHERE s.doctor.idDoctor = :doctorId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND s.isActive = true " +
           "AND s.effectiveFrom <= :endDate " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :startDate)")
    List<Schedule> findOverlappingSchedules(@Param("doctorId") Integer doctorId,
                                             @Param("dayOfWeek") Integer dayOfWeek,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
