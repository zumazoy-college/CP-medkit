package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCertificateRequest {

    @NotNull(message = "ID приема обязателен")
    private Integer appointmentId;

    @NotNull(message = "Тип справки обязателен")
    private String certificateType;

    private LocalDate validFrom;

    private LocalDate validTo;

    private LocalDate disabilityPeriodFrom;

    private LocalDate disabilityPeriodTo;

    private String workRestrictions;
}
