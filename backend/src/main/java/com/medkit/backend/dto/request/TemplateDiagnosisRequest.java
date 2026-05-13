package com.medkit.backend.dto.request;

import lombok.Data;

@Data
public class TemplateDiagnosisRequest {
    private Integer diagnosisId;
    private Boolean isPrimary;
}
