package com.medkit.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePatientRequest {

    private String firstName;

    private String lastName;

    private String middleName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат номера телефона")
    private String phoneNumber;

    private String allergies;

    private String chronicDiseases;
}
