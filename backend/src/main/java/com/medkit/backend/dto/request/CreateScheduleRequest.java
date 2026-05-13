package com.medkit.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateScheduleRequest {

    @NotNull(message = "День недели обязателен")
    @Min(value = 0, message = "День недели должен быть от 0 (воскресенье) до 6 (суббота)")
    @Max(value = 6, message = "День недели должен быть от 0 (воскресенье) до 6 (суббота)")
    private Integer dayOfWeek;

    @NotNull(message = "Время начала работы обязательно")
    private LocalTime startTime;

    @NotNull(message = "Время окончания работы обязательно")
    private LocalTime endTime;

    private LocalTime lunchStart;

    private LocalTime lunchEnd;

    @NotNull(message = "Длительность приема обязательна")
    @Min(value = 5, message = "Длительность приема должна быть не менее 5 минут")
    @Max(value = 120, message = "Длительность приема должна быть не более 120 минут")
    private Integer appointmentDuration = 30;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
