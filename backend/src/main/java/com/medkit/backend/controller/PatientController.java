package com.medkit.backend.controller;

import com.medkit.backend.dto.request.UpdatePatientRequest;
import com.medkit.backend.dto.request.UpdateUserSettingsRequest;
import com.medkit.backend.dto.response.PatientHistoryResponse;
import com.medkit.backend.dto.response.PatientResponse;
import com.medkit.backend.dto.response.UserSettingsResponse;
import com.medkit.backend.service.PatientService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Integer id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        PatientResponse patient = patientService.getPatientByEmail(userEmail);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> updateMyProfile(@Valid @RequestBody UpdatePatientRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        PatientResponse patient = patientService.updateMyProfile(userEmail, request);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/me/settings")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<UserSettingsResponse> getMySettings() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        UserSettingsResponse settings = patientService.getMySettings(userEmail);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/me/settings")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<UserSettingsResponse> updateMySettings(@Valid @RequestBody UpdateUserSettingsRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        UserSettingsResponse settings = patientService.updateMySettings(userEmail, request);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/me/avatar")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        PatientResponse patient = patientService.uploadAvatar(userEmail, file);
        return ResponseEntity.ok(patient);
    }

    @DeleteMapping("/me/avatar")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> deleteAvatar() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        PatientResponse patient = patientService.deleteAvatar(userEmail);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/snils/{snils}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientResponse> getPatientBySnils(@PathVariable String snils) {
        PatientResponse patient = patientService.getPatientBySnils(snils);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PatientResponse>> searchPatients(@RequestParam("q") String query) {
        List<PatientResponse> patients = patientService.searchPatients(query);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{patientId}/history")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<PatientHistoryResponse>> getPatientHistory(@PathVariable Integer patientId) {
        List<PatientHistoryResponse> history = patientService.getPatientHistory(patientId);
        return ResponseEntity.ok(history);
    }
}
