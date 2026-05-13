package com.medkit.backend.repository;

import com.medkit.backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findByPatient_IdPatientAndIsDeletedFalse(Integer patientId);

    Optional<Favorite> findByPatient_IdPatientAndDoctor_IdDoctorAndIsDeletedFalse(
        Integer patientId, Integer doctorId);

    Optional<Favorite> findByPatient_IdPatientAndDoctor_IdDoctor(
        Integer patientId, Integer doctorId);

    boolean existsByPatient_IdPatientAndDoctor_IdDoctorAndIsDeletedFalse(
        Integer patientId, Integer doctorId);
}
