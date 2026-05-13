package com.medkit.backend.controller;

import com.medkit.backend.dto.request.CreateReviewRequest;
import com.medkit.backend.dto.request.UpdateReviewRequest;
import com.medkit.backend.dto.request.ReportReviewRequest;
import com.medkit.backend.dto.response.ReviewResponse;
import com.medkit.backend.service.ReviewService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        ReviewResponse review = reviewService.createReview(userEmail, request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Page<ReviewResponse>> getDoctorReviews(
            @PathVariable Integer doctorId,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getDoctorReviews(doctorId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Integer reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        ReviewResponse review = reviewService.updateReview(userEmail, reviewId, request);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        reviewService.deleteReview(userEmail, reviewId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reviewId}/report")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> reportReview(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReportReviewRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        reviewService.reportReview(userEmail, reviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
