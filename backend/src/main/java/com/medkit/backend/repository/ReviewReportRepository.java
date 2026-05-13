package com.medkit.backend.repository;

import com.medkit.backend.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Integer> {
}
