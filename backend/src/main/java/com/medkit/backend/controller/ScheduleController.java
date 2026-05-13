package com.medkit.backend.controller;

import com.medkit.backend.dto.request.CreateScheduleExceptionRequest;
import com.medkit.backend.dto.request.CreateScheduleRequest;
import com.medkit.backend.dto.response.ScheduleExceptionResponse;
import com.medkit.backend.dto.response.ScheduleResponse;
import com.medkit.backend.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ScheduleResponse>> getDoctorSchedule(@PathVariable Integer doctorId) {
        List<ScheduleResponse> schedule = scheduleService.getDoctorSchedule(doctorId);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<ScheduleResponse>> getMySchedule() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<ScheduleResponse> schedule = scheduleService.getMySchedule(userEmail);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        ScheduleResponse response = scheduleService.createSchedule(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody CreateScheduleRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        ScheduleResponse response = scheduleService.updateSchedule(userEmail, scheduleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer scheduleId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        scheduleService.deleteSchedule(userEmail, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<ScheduleResponse>> batchUpdateSchedules(
            @Valid @RequestBody com.medkit.backend.dto.request.BatchUpdateScheduleRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<ScheduleResponse> responses = scheduleService.batchUpdateSchedules(userEmail, request);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/exceptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ScheduleExceptionResponse> createScheduleException(
            @Valid @RequestBody CreateScheduleExceptionRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        ScheduleExceptionResponse response = scheduleService.createScheduleException(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/doctor/{doctorId}/exceptions")
    public ResponseEntity<List<ScheduleExceptionResponse>> getDoctorScheduleExceptions(
            @PathVariable Integer doctorId) {
        List<ScheduleExceptionResponse> exceptions = scheduleService.getDoctorScheduleExceptions(doctorId);
        return ResponseEntity.ok(exceptions);
    }

    @GetMapping("/doctor/{doctorId}/exceptions/range")
    public ResponseEntity<List<ScheduleExceptionResponse>> getDoctorScheduleExceptionsByDateRange(
            @PathVariable Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ScheduleExceptionResponse> exceptions = scheduleService
                .getDoctorScheduleExceptionsByDateRange(doctorId, startDate, endDate);
        return ResponseEntity.ok(exceptions);
    }

    @GetMapping("/my/exceptions/range")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<ScheduleExceptionResponse>> getMyScheduleExceptionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<ScheduleExceptionResponse> exceptions = scheduleService
                .getMyScheduleExceptionsByDateRange(userEmail, startDate, endDate);
        return ResponseEntity.ok(exceptions);
    }

    @DeleteMapping("/exceptions/{exceptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteScheduleException(@PathVariable Integer exceptionId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        scheduleService.deleteScheduleException(userEmail, exceptionId);
        return ResponseEntity.noContent().build();
    }
}
