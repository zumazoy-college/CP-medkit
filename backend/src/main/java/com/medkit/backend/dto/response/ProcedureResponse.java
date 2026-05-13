package com.medkit.backend.dto.response;

import lombok.Data;

@Data
public class ProcedureResponse {

    private Integer idProcedure;
    private String title;
    private String description;
    private Integer duration;
}
