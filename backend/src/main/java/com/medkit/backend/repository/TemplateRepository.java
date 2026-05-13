package com.medkit.backend.repository;

import com.medkit.backend.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Integer> {

    List<Template> findByDoctor_IdDoctorOrderByCreatedAtDesc(Integer doctorId);
}
