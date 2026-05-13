package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateScheduleExceptionRequest {

    private String reason;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDate endDate;

    private Boolean forceBlock = false; // Flag to force blocking even with appointments
}
