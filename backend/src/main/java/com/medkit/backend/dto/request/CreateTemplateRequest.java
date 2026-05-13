package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateTemplateRequest {

    @NotBlank(message = "Название шаблона обязательно")
    private String title;

    private String complaints;
    private String anamnesis;
    private String examination;
    private String recommendations;

    private List<Integer> diagnosisIds;
    private List<TemplateDiagnosisRequest> diagnoses;
}
