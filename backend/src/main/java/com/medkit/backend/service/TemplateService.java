package com.medkit.backend.service;

import com.medkit.backend.dto.request.CreateTemplateRequest;
import com.medkit.backend.dto.request.TemplateDiagnosisRequest;
import com.medkit.backend.dto.request.UpdateTemplateRequest;
import com.medkit.backend.dto.response.DiagnosisResponse;
import com.medkit.backend.dto.response.TemplateResponse;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateDiagnosisRepository templateDiagnosisRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public TemplateResponse createTemplate(String userEmail, CreateTemplateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Template template = new Template();
        template.setDoctor(doctor);
        template.setTitle(request.getTitle());
        template.setComplaints(request.getComplaints());
        template.setAnamnesis(request.getAnamnesis());
        template.setObjectiveData(request.getExamination());
        template.setRecommendations(request.getRecommendations());

        Template savedTemplate = templateRepository.save(template);

        // Handle diagnoses - support both old format (diagnosisIds) and new format (diagnoses)
        if (request.getDiagnoses() != null && !request.getDiagnoses().isEmpty()) {
            for (TemplateDiagnosisRequest diagnosisRequest : request.getDiagnoses()) {
                Diagnosis diagnosis = diagnosisRepository.findById(diagnosisRequest.getDiagnosisId())
                        .orElseThrow(() -> new ResourceNotFoundException("Диагноз не найден: " + diagnosisRequest.getDiagnosisId()));

                TemplateDiagnosis templateDiagnosis = new TemplateDiagnosis();
                templateDiagnosis.setTemplate(savedTemplate);
                templateDiagnosis.setDiagnosis(diagnosis);
                templateDiagnosis.setIsPrimary(diagnosisRequest.getIsPrimary() != null ? diagnosisRequest.getIsPrimary() : false);
                templateDiagnosisRepository.save(templateDiagnosis);
            }
        } else if (request.getDiagnosisIds() != null) {
            // Fallback to old format for backward compatibility
            for (Integer diagnosisId : request.getDiagnosisIds()) {
                Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                        .orElseThrow(() -> new ResourceNotFoundException("Диагноз не найден: " + diagnosisId));

                TemplateDiagnosis templateDiagnosis = new TemplateDiagnosis();
                templateDiagnosis.setTemplate(savedTemplate);
                templateDiagnosis.setDiagnosis(diagnosis);
                templateDiagnosis.setIsPrimary(false);
                templateDiagnosisRepository.save(templateDiagnosis);
            }
        }

        return mapToResponse(savedTemplate);
    }

    @Transactional
    public TemplateResponse updateTemplate(String userEmail, Integer templateId, UpdateTemplateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Шаблон не найден"));

        if (!template.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new ResourceNotFoundException("Шаблон не найден");
        }

        // Update fields
        if (request.getTitle() != null) {
            template.setTitle(request.getTitle());
        }
        if (request.getComplaints() != null) {
            template.setComplaints(request.getComplaints());
        }
        if (request.getAnamnesis() != null) {
            template.setAnamnesis(request.getAnamnesis());
        }
        if (request.getExamination() != null) {
            template.setObjectiveData(request.getExamination());
        }
        if (request.getRecommendations() != null) {
            template.setRecommendations(request.getRecommendations());
        }

        Template updatedTemplate = templateRepository.save(template);

        // Update diagnoses if provided - support both old format (diagnosisIds) and new format (diagnoses)
        if (request.getDiagnoses() != null) {
            // Remove old diagnoses
            templateDiagnosisRepository.deleteByTemplate_IdTemplate(templateId);
            entityManager.flush();
            entityManager.clear();

            // Add new diagnoses with isPrimary flag
            for (TemplateDiagnosisRequest diagnosisRequest : request.getDiagnoses()) {
                Diagnosis diagnosis = diagnosisRepository.findById(diagnosisRequest.getDiagnosisId())
                        .orElseThrow(() -> new ResourceNotFoundException("Диагноз не найден: " + diagnosisRequest.getDiagnosisId()));

                TemplateDiagnosis templateDiagnosis = new TemplateDiagnosis();
                templateDiagnosis.setTemplate(updatedTemplate);
                templateDiagnosis.setDiagnosis(diagnosis);
                templateDiagnosis.setIsPrimary(diagnosisRequest.getIsPrimary() != null ? diagnosisRequest.getIsPrimary() : false);
                templateDiagnosisRepository.save(templateDiagnosis);
            }
        } else if (request.getDiagnosisIds() != null) {
            // Fallback to old format for backward compatibility
            // Remove old diagnoses
            templateDiagnosisRepository.deleteByTemplate_IdTemplate(templateId);
            entityManager.flush();
            entityManager.clear();

            // Add new diagnoses
            for (Integer diagnosisId : request.getDiagnosisIds()) {
                Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                        .orElseThrow(() -> new ResourceNotFoundException("Диагноз не найден: " + diagnosisId));

                TemplateDiagnosis templateDiagnosis = new TemplateDiagnosis();
                templateDiagnosis.setTemplate(updatedTemplate);
                templateDiagnosis.setDiagnosis(diagnosis);
                templateDiagnosis.setIsPrimary(false);
                templateDiagnosisRepository.save(templateDiagnosis);
            }
        }

        return mapToResponse(updatedTemplate);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getDoctorTemplates(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return templateRepository.findByDoctor_IdDoctorOrderByCreatedAtDesc(doctor.getIdDoctor())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(String userEmail, Integer templateId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Шаблон не найден"));

        if (!template.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new ResourceNotFoundException("Шаблон не найден");
        }

        return mapToResponse(template);
    }

    @Transactional
    public void deleteTemplate(String userEmail, Integer templateId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Шаблон не найден"));

        if (!template.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new ResourceNotFoundException("Шаблон не найден");
        }

        templateDiagnosisRepository.deleteByTemplate_IdTemplate(templateId);
        templateRepository.delete(template);
    }

    private TemplateResponse mapToResponse(Template template) {
        TemplateResponse response = new TemplateResponse();
        response.setIdTemplate(template.getIdTemplate());
        response.setId(template.getIdTemplate()); // Alias
        response.setDoctorId(template.getDoctor().getIdDoctor());
        response.setTitle(template.getTitle());
        response.setName(template.getTitle()); // Alias
        response.setComplaints(template.getComplaints());
        response.setAnamnesis(template.getAnamnesis());
        response.setExamination(template.getObjectiveData());
        response.setRecommendations(template.getRecommendations());
        response.setCreatedAt(template.getCreatedAt());

        List<DiagnosisResponse> diagnoses = templateDiagnosisRepository
                .findByTemplate_IdTemplate(template.getIdTemplate())
                .stream()
                .map(td -> {
                    DiagnosisResponse dr = new DiagnosisResponse();
                    dr.setIdDiagnosis(td.getDiagnosis().getIdDiagnosis());
                    dr.setIcdCode(td.getDiagnosis().getIcdCode());
                    dr.setName(td.getDiagnosis().getIcdName());
                    dr.setIsPrimary(td.getIsPrimary());
                    return dr;
                })
                .collect(Collectors.toList());
        response.setDiagnoses(diagnoses);

        // Create simplified diagnosis string for frontend
        if (!diagnoses.isEmpty()) {
            String diagnosisString = diagnoses.stream()
                    .map(d -> d.getIcdCode() + " - " + d.getName())
                    .collect(Collectors.joining(", "));
            response.setDiagnosis(diagnosisString);
        }

        return response;
    }
}
