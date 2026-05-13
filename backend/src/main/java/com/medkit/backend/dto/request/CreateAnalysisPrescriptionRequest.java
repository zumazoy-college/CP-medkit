package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAnalysisPrescriptionRequest {

    @NotNull(message = "ID приема обязателен")
    private Integer appointmentId;

    @NotNull(message = "ID анализа обязателен")
    private Integer analysisId;

    private String instructions;
}
