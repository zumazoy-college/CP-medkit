package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateSlotRequest {

    @NotNull(message = "Дата обязательна")
    private LocalDate slotDate;

    @NotNull(message = "Время начала обязательно")
    private LocalTime startTime;

    @NotNull(message = "Время окончания обязательно")
    private LocalTime endTime;
}
