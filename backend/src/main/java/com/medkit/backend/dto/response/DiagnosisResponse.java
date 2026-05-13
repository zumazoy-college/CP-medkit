package com.medkit.backend.dto.response;

import lombok.Data;

@Data
public class DiagnosisResponse {

    private Integer idDiagnosis;
    private String icdCode;
    private String name;
    private String description;
    private Boolean isPrimary;
}
