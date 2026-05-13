package com.medkit.backend.repository;

import com.medkit.backend.entity.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Integer> {

    Optional<Analysis> findByCode(String code);

    Page<Analysis> findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCase(
        String title, String code, Pageable pageable);
}
