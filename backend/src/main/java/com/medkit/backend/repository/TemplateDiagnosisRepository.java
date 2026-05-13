package com.medkit.backend.repository;

import com.medkit.backend.entity.TemplateDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateDiagnosisRepository extends JpaRepository<TemplateDiagnosis, Integer> {

    List<TemplateDiagnosis> findByTemplate_IdTemplate(Integer templateId);

    void deleteByTemplate_IdTemplate(Integer templateId);
}
