package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemplateResponse {

    private Integer idTemplate;
    private Integer id; // Alias for frontend compatibility
    private Integer doctorId;
    private String title;
    private String name; // Alias for frontend compatibility
    private String complaints;
    private String anamnesis;
    private String examination;
    private String recommendations;
    private List<DiagnosisResponse> diagnoses;
    private String diagnosis; // Simplified diagnosis string for frontend
    private LocalDateTime createdAt;
}
