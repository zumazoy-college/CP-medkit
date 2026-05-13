package com.medkit.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    private Integer idCertificate;
    private Integer appointmentId;
    private Integer patientId;
    private String patientFullName;
    private Integer doctorId;
    private String doctorFullName;
    private String doctorSpecialization;
    private String certificateType;
    private String certificateTypeName;
    private String filePath;
    private LocalDate validFrom;
    private LocalDate validTo;
    private LocalDate disabilityPeriodFrom;
    private LocalDate disabilityPeriodTo;
    private String workRestrictions;
    private LocalDateTime createdAt;
}
