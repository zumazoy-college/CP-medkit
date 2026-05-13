package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnalysisPrescriptionResponse {

    private Integer id;
    private Integer appointmentId;
    private Integer analysisId;
    private String analysisName;
    private String instructions;
    private String status;
    private LocalDateTime createdAt;
}
