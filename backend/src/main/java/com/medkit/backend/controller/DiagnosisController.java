package com.medkit.backend.controller;

import com.medkit.backend.dto.request.AddDiagnosisRequest;
import com.medkit.backend.dto.response.AppointmentDiagnosisResponse;
import com.medkit.backend.dto.response.DiagnosisResponse;
import com.medkit.backend.service.DiagnosisService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping("/appointment")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDiagnosisResponse> addDiagnosisToAppointment(
            @Valid @RequestBody AddDiagnosisRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AppointmentDiagnosisResponse response = diagnosisService.addDiagnosisToAppointment(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<AppointmentDiagnosisResponse>> getAppointmentDiagnoses(
            @PathVariable Integer appointmentId) {
        List<AppointmentDiagnosisResponse> diagnoses = diagnosisService.getAppointmentDiagnoses(appointmentId);
        return ResponseEntity.ok(diagnoses);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDiagnosisResponse>> getMyDiagnoses() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<AppointmentDiagnosisResponse> diagnoses = diagnosisService.getPatientDiagnoses(userEmail);
        return ResponseEntity.ok(diagnoses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<DiagnosisResponse>> searchDiagnoses(
            @RequestParam String query,
            Pageable pageable) {
        Page<DiagnosisResponse> diagnoses = diagnosisService.searchDiagnoses(query, pageable);
        return ResponseEntity.ok(diagnoses);
    }

    @GetMapping("/code/{icdCode}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DiagnosisResponse> getDiagnosisByCode(@PathVariable String icdCode) {
        DiagnosisResponse diagnosis = diagnosisService.getDiagnosisByCode(icdCode);
        return ResponseEntity.ok(diagnosis);
    }

    @DeleteMapping("/appointment/{appointmentDiagnosisId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> removeDiagnosisFromAppointment(@PathVariable Integer appointmentDiagnosisId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        diagnosisService.removeDiagnosisFromAppointment(userEmail, appointmentDiagnosisId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/appointment/{appointmentDiagnosisId}/primary")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDiagnosisResponse> setPrimaryDiagnosis(@PathVariable Integer appointmentDiagnosisId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AppointmentDiagnosisResponse response = diagnosisService.setPrimaryDiagnosis(userEmail, appointmentDiagnosisId);
        return ResponseEntity.ok(response);
    }
}
