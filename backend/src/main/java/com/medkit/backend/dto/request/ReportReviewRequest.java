package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportReviewRequest {

    @NotBlank(message = "Причина жалобы обязательна")
    private String reason;

    private String description;
}
