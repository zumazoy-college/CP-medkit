package com.medkit.backend.controller;

import com.medkit.backend.dto.request.UpdateDoctorRequest;
import com.medkit.backend.dto.request.UpdateNotificationSettingsRequest;
import com.medkit.backend.dto.response.DoctorResponse;
import com.medkit.backend.dto.response.DoctorStatsResponse;
import com.medkit.backend.dto.response.PatientResponse;
import com.medkit.backend.dto.response.ReviewResponse;
import com.medkit.backend.service.DoctorService;
import com.medkit.backend.service.PatientService;
import com.medkit.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Integer id) {
        DoctorResponse doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        DoctorResponse doctor = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(doctor);
    }

    @PatchMapping("/{id}/notification-settings")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorResponse> updateNotificationSettings(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateNotificationSettingsRequest request) {
        DoctorResponse doctor = doctorService.updateNotificationSettings(
                id,
                request.getNotifyCancellations(),
                request.getNotifyBookings()
        );
        return ResponseEntity.ok(doctor);
    }

    @PostMapping("/{id}/avatar")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @PathVariable Integer id,
            @RequestParam("avatar") MultipartFile file) {
        Map<String, String> response = doctorService.uploadAvatar(id, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/avatar")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteAvatar(@PathVariable Integer id) {
        doctorService.deleteAvatar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(Pageable pageable) {
        Page<DoctorResponse> doctors = doctorService.getAllDoctors(pageable);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DoctorResponse>> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minExperience,
            Pageable pageable) {

        // Map sort fields from DTO to database column names
        org.springframework.data.domain.Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            java.util.List<org.springframework.data.domain.Sort.Order> orders = new java.util.ArrayList<>();
            for (org.springframework.data.domain.Sort.Order order : sort) {
                String property = order.getProperty();
                org.springframework.data.domain.Sort.Direction direction = order.getDirection();

                // Map DTO field names to database column names (without prefix - Spring adds it)
                switch (property) {
                    case "experienceYears":
                        // Sort by work_experience date: earlier date = more experience
                        // So DESC experience years = ASC work_experience date
                        property = "work_experience";
                        direction = direction == org.springframework.data.domain.Sort.Direction.DESC
                                ? org.springframework.data.domain.Sort.Direction.ASC
                                : org.springframework.data.domain.Sort.Direction.DESC;
                        break;
                    case "nextAvailableSlotDate":
                        property = "next_available_slot_date";
                        break;
                    case "nextAvailableSlotTime":
                        property = "next_available_slot_time";
                        break;
                    case "lastName":
                    case "user.lastName":
                        property = "last_name";
                        break;
                    case "firstName":
                    case "user.firstName":
                        property = "first_name";
                        break;
                    case "rating":
                        property = "rating";
                        break;
                    default:
                        // Keep property as is
                        break;
                }
                orders.add(new org.springframework.data.domain.Sort.Order(direction, property));
            }
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(orders)
            );
        } else {
            // Set default sort to rating descending if no sort specified
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "rating")
            );
        }

        Page<DoctorResponse> doctors = doctorService.searchDoctors(specialization, minRating, name, gender, minExperience, pageable);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<DoctorResponse>> getTopRatedDoctors() {
        List<DoctorResponse> doctors = doctorService.getTopRatedDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<DoctorStatsResponse> getDoctorStats(@PathVariable Integer id) {
        DoctorStatsResponse stats = doctorService.getDoctorStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{doctorId}/patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PatientResponse>> getDoctorPatients(@PathVariable Integer doctorId) {
        List<PatientResponse> patients = patientService.getDoctorPatients(doctorId);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{doctorId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getDoctorReviews(
            @PathVariable Integer doctorId,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getDoctorReviews(doctorId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{doctorId}/rating-stats")
    public ResponseEntity<Map<String, Object>> getDoctorRatingStats(@PathVariable Integer doctorId) {
        Map<String, Object> stats = reviewService.getDoctorRatingStats(doctorId);
        return ResponseEntity.ok(stats);
    }
}
