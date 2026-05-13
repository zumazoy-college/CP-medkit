package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class DetailedAppointmentResponse {

    private Integer idAppointment;
    private Integer slotId;
    private PatientInfo patient;
    private DoctorInfo doctor;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;

    private String complaints;
    private String anamnesis;
    private String objectiveData;
    private String recommendations;

    private List<AppointmentDiagnosisResponse> diagnoses;
    private List<MedicationPrescriptionResponse> medications;
    private List<ProcedurePrescriptionResponse> procedures;
    private List<AnalysisPrescriptionResponse> analyses;
    private List<FileResponse> files;

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

    @Data
    public static class DoctorInfo {
        private Integer id;
        private String firstName;
        private String lastName;
        private String middleName;
        private String specialization;
    }
}
