package com.medkit.backend.controller;

import com.medkit.backend.dto.request.CreateAnalysisPrescriptionRequest;
import com.medkit.backend.dto.request.CreateMedicationPrescriptionRequest;
import com.medkit.backend.dto.request.CreateProcedurePrescriptionRequest;
import com.medkit.backend.dto.response.AnalysisPrescriptionResponse;
import com.medkit.backend.dto.response.MedicationPrescriptionResponse;
import com.medkit.backend.dto.response.ProcedurePrescriptionResponse;
import com.medkit.backend.service.PrescriptionService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/medications")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicationPrescriptionResponse> createMedicationPrescription(
            @Valid @RequestBody CreateMedicationPrescriptionRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        MedicationPrescriptionResponse response = prescriptionService.createMedicationPrescription(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/procedures")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ProcedurePrescriptionResponse> createProcedurePrescription(
            @Valid @RequestBody CreateProcedurePrescriptionRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        ProcedurePrescriptionResponse response = prescriptionService.createProcedurePrescription(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/analyses")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AnalysisPrescriptionResponse> createAnalysisPrescription(
            @Valid @RequestBody CreateAnalysisPrescriptionRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AnalysisPrescriptionResponse response = prescriptionService.createAnalysisPrescription(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/medications/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicationPrescriptionResponse>> getMyMedicationPrescriptions() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<MedicationPrescriptionResponse> prescriptions = prescriptionService.getPatientMedicationPrescriptions(userEmail);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/procedures/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ProcedurePrescriptionResponse>> getMyProcedurePrescriptions() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<ProcedurePrescriptionResponse> prescriptions = prescriptionService.getPatientProcedurePrescriptions(userEmail);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/analyses/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AnalysisPrescriptionResponse>> getMyAnalysisPrescriptions() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<AnalysisPrescriptionResponse> prescriptions = prescriptionService.getPatientAnalysisPrescriptions(userEmail);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/medications/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<MedicationPrescriptionResponse>> getAppointmentMedicationPrescriptions(
            @PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<MedicationPrescriptionResponse> prescriptions = prescriptionService.getAppointmentMedicationPrescriptions(userEmail, appointmentId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/procedures/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<ProcedurePrescriptionResponse>> getAppointmentProcedurePrescriptions(
            @PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<ProcedurePrescriptionResponse> prescriptions = prescriptionService.getAppointmentProcedurePrescriptions(userEmail, appointmentId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/analyses/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<AnalysisPrescriptionResponse>> getAppointmentAnalysisPrescriptions(
            @PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<AnalysisPrescriptionResponse> prescriptions = prescriptionService.getAppointmentAnalysisPrescriptions(userEmail, appointmentId);
        return ResponseEntity.ok(prescriptions);
    }

    @DeleteMapping("/medications/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteMedicationPrescription(@PathVariable Integer prescriptionId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        prescriptionService.deleteMedicationPrescription(userEmail, prescriptionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/procedures/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteProcedurePrescription(@PathVariable Integer prescriptionId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        prescriptionService.deleteProcedurePrescription(userEmail, prescriptionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/analyses/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteAnalysisPrescription(@PathVariable Integer prescriptionId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        prescriptionService.deleteAnalysisPrescription(userEmail, prescriptionId);
        return ResponseEntity.noContent().build();
    }
}
