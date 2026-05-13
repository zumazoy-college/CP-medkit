package com.medkit.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTemplateRequest {

    private String title;
    private String complaints;
    private String anamnesis;
    private String examination;
    private String recommendations;
    private List<Integer> diagnosisIds;
    private List<TemplateDiagnosisRequest> diagnoses;
}
