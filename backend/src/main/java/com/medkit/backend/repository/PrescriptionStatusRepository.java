package com.medkit.backend.repository;

import com.medkit.backend.entity.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionStatusRepository extends JpaRepository<PrescriptionStatus, Integer> {

    Optional<PrescriptionStatus> findByTitle(String title);
}
