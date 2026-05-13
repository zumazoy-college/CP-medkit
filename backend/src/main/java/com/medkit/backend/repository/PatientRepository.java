package com.medkit.backend.repository;

import com.medkit.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Optional<Patient> findByUser_IdUser(Integer userId);

    Optional<Patient> findBySnils(String snils);

    boolean existsBySnils(String snils);

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(CONCAT(p.user.lastName, ' ', p.user.firstName, ' ', COALESCE(p.user.middleName, ''))) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR p.snils LIKE CONCAT('%', :query, '%') " +
           "OR CAST(p.birthdate AS string) LIKE CONCAT('%', :query, '%')")
    List<Patient> searchPatients(@Param("query") String query);

    @Query("SELECT DISTINCT p FROM Patient p " +
           "JOIN p.appointments a " +
           "JOIN a.slot s " +
           "WHERE s.doctor.idDoctor = :doctorId")
    List<Patient> findByDoctorId(@Param("doctorId") Integer doctorId);
}
