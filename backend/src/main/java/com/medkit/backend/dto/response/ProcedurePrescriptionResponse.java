package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcedurePrescriptionResponse {

    private Integer id;
    private Integer appointmentId;
    private Integer procedureId;
    private String procedureName;
    private String instructions;
    private String status;
    private LocalDateTime createdAt;
}
