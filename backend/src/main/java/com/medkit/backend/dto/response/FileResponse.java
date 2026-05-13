package com.medkit.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileResponse {

    private Integer id;
    private Integer appointmentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
