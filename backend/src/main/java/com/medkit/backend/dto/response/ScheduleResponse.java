package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleResponse {

    private Integer idSchedule;
    private Integer doctorId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime lunchStart;
    private LocalTime lunchEnd;
    private Integer appointmentDuration;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
