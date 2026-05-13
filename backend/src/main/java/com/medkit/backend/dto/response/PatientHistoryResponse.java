package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PatientHistoryResponse {

    private Integer id;
    private Integer patientId;
    private Integer appointmentId;
    private LocalDateTime date;
    private String diagnosis;
    private String prescriptions;
    private String notes;
    private DoctorInfo doctor;

    @Data
    public static class DoctorInfo {
        private Integer id;
        private String firstName;
        private String lastName;
        private String middleName;
        private String specialization;
    }
}
