package com.medkit.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotResponse {

    private Integer idSlot;
    private Integer doctorId;
    private String doctorName;
    private Integer patientId;
    private String patientName;
    private Integer appointmentId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String cancellationReason;
    private LocalDateTime createdAt;
}
