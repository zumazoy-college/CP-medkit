package com.medkit.backend.repository;

import com.medkit.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Page<Review> findByDoctor_IdDoctorOrderByCreatedAtDesc(Integer doctorId, Pageable pageable);

    Optional<Review> findByAppointment_IdAppointment(Integer appointmentId);

    boolean existsByAppointment_IdAppointment(Integer appointmentId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.idDoctor = :doctorId")
    Double calculateAverageRating(@Param("doctorId") Integer doctorId);

    long countByDoctor_IdDoctor(Integer doctorId);

    long countByDoctor_IdDoctorAndRating(Integer doctorId, Integer rating);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.idReview = :reviewId")
    void deleteByIdReview(@Param("reviewId") Integer reviewId);
}
