package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Integer idNotification;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private String link;
    private LocalDateTime createdAt;
}
