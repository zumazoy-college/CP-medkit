package com.medkit.backend.repository;

import com.medkit.backend.entity.Procedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Integer> {

    Page<Procedure> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
