package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicationPrescriptionResponse {

    private Integer id;
    private Integer appointmentId;
    private Integer medicationId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private String status;
    private LocalDateTime createdAt;
}
