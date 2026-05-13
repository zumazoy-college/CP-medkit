package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GenerateSlotsRequest {

    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDate endDate;

    @NotNull(message = "Длительность слота обязательна")
    private Integer slotDurationMinutes = 30;
}
