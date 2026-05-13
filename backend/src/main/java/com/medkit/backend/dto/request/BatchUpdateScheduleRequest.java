package com.medkit.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BatchUpdateScheduleRequest {

    @NotNull(message = "Дата начала действия обязательна")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotEmpty(message = "Необходимо указать хотя бы один день")
    @Valid
    private List<CreateScheduleRequest> schedules;
}
