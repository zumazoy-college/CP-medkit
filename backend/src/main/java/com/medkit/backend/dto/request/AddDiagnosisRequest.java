package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddDiagnosisRequest {

    @NotNull(message = "ID приема обязателен")
    private Integer appointmentId;

    @NotNull(message = "ID диагноза обязателен")
    private Integer diagnosisId;

    private Boolean isPrimary;

    private String notes;
}
