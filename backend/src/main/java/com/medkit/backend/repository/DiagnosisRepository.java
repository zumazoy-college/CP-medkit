package com.medkit.backend.repository;

import com.medkit.backend.entity.Diagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Integer> {

    Optional<Diagnosis> findByIcdCode(String icdCode);

    Page<Diagnosis> findByIcdNameContainingIgnoreCaseOrIcdCodeContainingIgnoreCase(
        String name, String code, Pageable pageable);
}
