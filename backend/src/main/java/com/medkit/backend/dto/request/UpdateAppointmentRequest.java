package com.medkit.backend.dto.request;

import lombok.Data;

@Data
public class UpdateAppointmentRequest {

    private String complaints;
    private String anamnesis;
    private String objectiveData;
    private String recommendations;
}
