package com.medkit.backend.service;

import com.medkit.backend.dto.request.CreateReviewRequest;
import com.medkit.backend.dto.request.UpdateReviewRequest;
import com.medkit.backend.dto.request.ReportReviewRequest;
import com.medkit.backend.dto.response.ReviewResponse;
import com.medkit.backend.entity.Appointment;
import com.medkit.backend.entity.Doctor;
import com.medkit.backend.entity.Patient;
import com.medkit.backend.entity.Review;
import com.medkit.backend.entity.ReviewReport;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.AppointmentRepository;
import com.medkit.backend.repository.DoctorRepository;
import com.medkit.backend.repository.PatientRepository;
import com.medkit.backend.repository.ReviewRepository;
import com.medkit.backend.repository.ReviewReportRepository;
import com.medkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ReviewReportRepository reviewReportRepository;

    @Transactional
    public ReviewResponse createReview(String userEmail, CreateReviewRequest request) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if (!appointment.getPatient().getIdPatient().equals(patient.getIdPatient())) {
            throw new BadRequestException("Вы можете оставить отзыв только на свой прием");
        }

        if (!"completed".equals(appointment.getSlot().getStatus().getTitle())) {
            throw new BadRequestException("Отзыв можно оставить только на завершенный прием");
        }

        if (reviewRepository.existsByAppointment_IdAppointment(request.getAppointmentId())) {
            throw new BadRequestException("Отзыв на этот прием уже существует");
        }

        Review review = new Review();
        review.setAppointment(appointment);
        review.setPatient(patient);
        review.setDoctor(appointment.getSlot().getDoctor());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        updateDoctorRating(appointment.getSlot().getDoctor().getIdDoctor());

        return mapToResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getDoctorReviews(Integer doctorId, Pageable pageable) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return reviewRepository.findByDoctor_IdDoctorOrderByCreatedAtDesc(doctorId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ReviewResponse updateReview(String userEmail, Integer reviewId, UpdateReviewRequest request) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));

        if (!review.getPatient().getIdPatient().equals(patient.getIdPatient())) {
            throw new BadRequestException("Вы можете редактировать только свой отзыв");
        }

        // Check if review can be edited (within 24 hours of creation)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = review.getCreatedAt();
        long hoursSinceCreation = java.time.Duration.between(createdAt, now).toHours();

        if (hoursSinceCreation >= 24) {
            throw new BadRequestException("Отзыв можно редактировать только в течение 24 часов после создания");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        updateDoctorRating(review.getDoctor().getIdDoctor());

        return mapToResponse(savedReview);
    }

    @Transactional
    public void deleteReview(String userEmail, Integer reviewId) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));

        if (!review.getPatient().getIdPatient().equals(patient.getIdPatient())) {
            throw new BadRequestException("Вы можете удалить только свой отзыв");
        }

        // Check if review can be deleted (within 24 hours of creation)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = review.getCreatedAt();
        long hoursSinceCreation = java.time.Duration.between(createdAt, now).toHours();

        if (hoursSinceCreation >= 24) {
            throw new BadRequestException("Отзыв можно удалить только в течение 24 часов после создания");
        }

        Integer doctorId = review.getDoctor().getIdDoctor();

        reviewRepository.deleteByIdReview(reviewId);
        reviewRepository.flush();

        updateDoctorRating(doctorId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDoctorRatingStats(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Double avgRating = reviewRepository.calculateAverageRating(doctorId);
        Long totalReviews = reviewRepository.countByDoctor_IdDoctor(doctorId);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            Long count = reviewRepository.countByDoctor_IdDoctorAndRating(doctorId, i);
            distribution.put(i, count);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", avgRating != null ? avgRating : 0.0);
        stats.put("totalReviews", totalReviews);
        stats.put("distribution", distribution);

        return stats;
    }

    @Transactional
    public void reportReview(String userEmail, Integer reviewId, ReportReviewRequest request) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));

        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporter(user);
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus("pending");

        reviewReportRepository.save(report);
    }

    private void updateDoctorRating(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Double avgRating = reviewRepository.calculateAverageRating(doctorId);
        Long reviewsCount = reviewRepository.countByDoctor_IdDoctor(doctorId);

        doctor.setRating(avgRating != null ? BigDecimal.valueOf(avgRating) : BigDecimal.ZERO);
        doctor.setReviewsCount(reviewsCount.intValue());
        doctorRepository.save(doctor);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setIdReview(review.getIdReview());
        response.setAppointmentId(review.getAppointment().getIdAppointment());
        response.setPatientId(review.getPatient().getIdPatient());
        response.setPatientName(review.getPatient().getUser().getLastName() + " " +
                               review.getPatient().getUser().getFirstName());
        response.setDoctorId(review.getDoctor().getIdDoctor());
        response.setRating(review.getRating().intValue());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setAppointmentDate(review.getAppointment().getSlot().getSlotDate());

        // Check if review can be deleted (within 24 hours of creation)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = review.getCreatedAt();
        long hoursSinceCreation = java.time.Duration.between(createdAt, now).toHours();
        response.setCanDelete(hoursSinceCreation < 24);
        response.setCanEdit(hoursSinceCreation < 24);

        return response;
    }
}
