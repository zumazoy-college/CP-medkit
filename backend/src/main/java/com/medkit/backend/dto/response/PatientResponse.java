package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientResponse {

    private Integer idPatient;
    private Integer id; // Alias for frontend compatibility
    private Integer userId;
    private String email;
    private String lastName;
    private String firstName;
    private String middleName;
    private String phoneNumber;
    private String phone; // Alias for frontend compatibility
    private String avatarUrl;
    private LocalDate birthdate;
    private LocalDate dateOfBirth; // Alias for frontend compatibility
    private String gender;
    private String snils;
    private String allergies;
    private String chronicDiseases;
    private String address;
    private String bloodType;
    private LocalDate lastAppointmentDate;
}
