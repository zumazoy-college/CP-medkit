package com.medkit.backend.dto.response;

import lombok.Data;

@Data
public class AnalysisResponse {

    private Integer idAnalysis;
    private String code;
    private String title;
    private String description;
}
