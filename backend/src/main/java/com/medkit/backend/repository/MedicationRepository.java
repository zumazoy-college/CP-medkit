package com.medkit.backend.repository;

import com.medkit.backend.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Integer> {

    Page<Medication> findByTitleContainingIgnoreCaseOrActiveSubstanceContainingIgnoreCase(
        String title, String activeSubstance, Pageable pageable);
}
