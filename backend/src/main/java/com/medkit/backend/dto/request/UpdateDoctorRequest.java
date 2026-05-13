package com.medkit.backend.dto.request;

import lombok.Data;

@Data
public class UpdateDoctorRequest {
    private String firstName;
    private String lastName;
    private String middleName;
    private String phoneNumber;
}
