package com.medkit.backend.dto.response;

import lombok.Data;

@Data
public class MedicationResponse {

    private Integer idMedication;
    private String title;
    private String activeSubstance;
    private String manufacturer;
    private String form;
}
