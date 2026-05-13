package com.medkit.backend.controller;

import com.medkit.backend.dto.request.CreateSlotRequest;
import com.medkit.backend.dto.request.GenerateSlotsRequest;
import com.medkit.backend.dto.response.AppointmentSlotResponse;
import com.medkit.backend.service.SlotManagementService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotManagementController {

    private final SlotManagementService slotManagementService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentSlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AppointmentSlotResponse response = slotManagementService.createSlot(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentSlotResponse>> generateSlots(@Valid @RequestBody GenerateSlotsRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<AppointmentSlotResponse> slots = slotManagementService.generateSlots(userEmail, request);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/my/range")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentSlotResponse>> getMySlotsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<AppointmentSlotResponse> slots = slotManagementService.getDoctorSlotsInRange(userEmail, startDate, endDate);
        return ResponseEntity.ok(slots);
    }

    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteSlot(@PathVariable Integer slotId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        slotManagementService.deleteSlot(userEmail, slotId);
        return ResponseEntity.noContent().build();
    }
}
