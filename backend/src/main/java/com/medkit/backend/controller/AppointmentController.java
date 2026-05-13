package com.medkit.backend.controller;

import com.medkit.backend.dto.request.UpdateAppointmentRequest;
import com.medkit.backend.dto.response.AppointmentResponse;
import com.medkit.backend.dto.response.AppointmentSlotResponse;
import com.medkit.backend.dto.response.DetailedAppointmentResponse;
import com.medkit.backend.service.AppointmentService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/slots")
    public ResponseEntity<List<AppointmentSlotResponse>> getAvailableSlots(
            @RequestParam Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentSlotResponse> slots = appointmentService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentSlotResponse> bookAppointment(
            @RequestParam Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AppointmentSlotResponse slot = appointmentService.bookAppointment(userEmail, doctorId, slotDate, startTime);
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/{slotId}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable Integer slotId,
            @RequestParam String reason) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        appointmentService.cancelAppointment(userEmail, slotId, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Page<AppointmentResponse>> getMyAppointments(Pageable pageable) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        Page<AppointmentResponse> appointments = appointmentService.getPatientAppointments(userEmail, pageable);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentResponse>> getDoctorAppointments(Pageable pageable) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        Page<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(userEmail, pageable);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/my/date")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointmentsByDate(userEmail, date);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AppointmentResponse appointment = appointmentService.completeAppointment(userEmail, appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<DetailedAppointmentResponse> getAppointmentById(@PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        DetailedAppointmentResponse appointment = appointmentService.getAppointmentById(userEmail, appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DetailedAppointmentResponse> updateAppointment(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        DetailedAppointmentResponse appointment = appointmentService.updateAppointment(userEmail, appointmentId, request);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DetailedAppointmentResponse>> getPatientHistory(@PathVariable Integer patientId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<DetailedAppointmentResponse> appointments = appointmentService.getPatientHistory(userEmail, patientId);
        return ResponseEntity.ok(appointments);
    }
}
