package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {

    private Integer idReview;
    private Integer appointmentId;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDate appointmentDate;
    private Boolean canDelete;
    private Boolean canEdit;
}
