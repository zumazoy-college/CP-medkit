package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteResponse {

    private Integer idFavorite;
    private Integer doctorId;
    private String doctorName;
    private String specialization;
    private Double rating;
    private String avatarUrl;
    private String nextAvailableSlot;
    private LocalDateTime createdAt;
}
