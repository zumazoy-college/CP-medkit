package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleExceptionResponse {

    private Integer idException;
    private Integer doctorId;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
}
