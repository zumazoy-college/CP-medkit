package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProcedurePrescriptionRequest {

    @NotNull(message = "ID приема обязателен")
    private Integer appointmentId;

    @NotNull(message = "ID процедуры обязателен")
    private Integer procedureId;

    private String instructions;
}
