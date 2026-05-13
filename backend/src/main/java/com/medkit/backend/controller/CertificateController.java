package com.medkit.backend.controller;

import com.medkit.backend.dto.request.CreateCertificateRequest;
import com.medkit.backend.dto.response.CertificateResponse;
import com.medkit.backend.service.CertificateService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<CertificateResponse> createCertificate(
            @Valid @RequestBody CreateCertificateRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        CertificateResponse certificate = certificateService.createCertificate(userEmail, request);
        return ResponseEntity.ok(certificate);
    }

    @GetMapping("/patient/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<CertificateResponse>> getMyPatientCertificates() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<CertificateResponse> certificates = certificateService.getPatientCertificates(userEmail);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<CertificateResponse>> getAppointmentCertificates(
            @PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<CertificateResponse> certificates = certificateService.getAppointmentCertificates(userEmail, appointmentId);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/{certificateId}/download")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Integer certificateId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        byte[] pdfBytes = certificateService.downloadCertificate(userEmail, certificateId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate_" + certificateId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
