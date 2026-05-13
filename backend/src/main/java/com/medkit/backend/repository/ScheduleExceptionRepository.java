package com.medkit.backend.repository;

import com.medkit.backend.entity.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Integer> {

    List<ScheduleException> findByDoctor_IdDoctorOrderByStartDateDesc(Integer doctorId);

    @Query("SELECT se FROM ScheduleException se WHERE se.doctor.idDoctor = :doctorId " +
           "AND se.startDate <= :date AND se.endDate >= :date")
    List<ScheduleException> findByDoctorAndDate(@Param("doctorId") Integer doctorId,
                                                  @Param("date") LocalDate date);

    @Query("SELECT se FROM ScheduleException se WHERE se.doctor.idDoctor = :doctorId " +
           "AND ((se.startDate BETWEEN :startDate AND :endDate) " +
           "OR (se.endDate BETWEEN :startDate AND :endDate) " +
           "OR (se.startDate <= :startDate AND se.endDate >= :endDate))")
    List<ScheduleException> findByDoctorAndDateRange(@Param("doctorId") Integer doctorId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}
