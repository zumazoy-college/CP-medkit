package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentDiagnosisResponse {

    private Integer id;
    private Integer appointmentId;
    private Integer diagnosisId;
    private String icdCode;
    private String diagnosisName;
    private Boolean isPrimary;
    private LocalDate appointmentDate;
}
