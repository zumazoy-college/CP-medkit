package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMedicationPrescriptionRequest {

    @NotNull(message = "ID приема обязателен")
    private Integer appointmentId;

    @NotNull(message = "ID лекарства обязателен")
    private Integer medicationId;

    @NotBlank(message = "Дозировка обязательна")
    private String dosage;

    @NotBlank(message = "Частота приема обязательна")
    private String frequency;

    @NotNull(message = "Длительность обязательна")
    private Integer duration;

    private String instructions;
}
