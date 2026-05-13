package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AppointmentResponse {

    private Integer idAppointment;
    private Integer slotId;
    private Integer patientId;
    private String patientName;
    private PatientInfo patient;
    private Integer doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorOffice;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Boolean hasReview;
    private Integer reviewId;
    private Short reviewRating;
    private String reviewComment;
    private Boolean canEditReview;
    private Boolean canDeleteReview;
    private String primaryDiagnosisName;
    private LocalDateTime createdAt;

    @Data
    public static class PatientInfo {
        private Integer id;
        private String firstName;
        private String lastName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String snils;
    }
}
