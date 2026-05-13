package com.medkit.backend.repository;

import com.medkit.backend.entity.Doctor;
import com.medkit.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    Optional<Doctor> findByUser_IdUser(Integer userId);

    Page<Doctor> findBySpecializationContainingIgnoreCase(String specialization, Pageable pageable);

    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN users u ON u.id_user = d.user_id " +
           "WHERE (:name IS NULL OR (LOWER(CONCAT(u.last_name, ' ', u.first_name, ' ', COALESCE(u.middle_name, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.rating >= :minRating) " +
           "AND (:genderId IS NULL OR u.gender_id = :genderId) " +
           "AND (COALESCE(:maxWorkExperienceDate, '9999-12-31'::date) = '9999-12-31'::date OR d.work_experience <= :maxWorkExperienceDate) " +
           "ORDER BY COALESCE(d.next_available_slot_date, '9999-12-31'::date) ASC, COALESCE(d.next_available_slot_time, '23:59:59'::time) ASC",
           countQuery = "SELECT COUNT(*) FROM doctors d " +
           "JOIN users u ON u.id_user = d.user_id " +
           "WHERE (:name IS NULL OR (LOWER(CONCAT(u.last_name, ' ', u.first_name, ' ', COALESCE(u.middle_name, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.rating >= :minRating) " +
           "AND (:genderId IS NULL OR u.gender_id = :genderId) " +
           "AND (COALESCE(:maxWorkExperienceDate, '9999-12-31'::date) = '9999-12-31'::date OR d.work_experience <= :maxWorkExperienceDate)",
           nativeQuery = true)
    Page<Doctor> findByFiltersOrderBySlot(@Param("specialization") String specialization,
                                           @Param("minRating") Double minRating,
                                           @Param("name") String name,
                                           @Param("genderId") Integer genderId,
                                           @Param("maxWorkExperienceDate") LocalDate maxWorkExperienceDate,
                                           Pageable pageable);

    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN users u ON u.id_user = d.user_id " +
           "WHERE (:name IS NULL OR (LOWER(CONCAT(u.last_name, ' ', u.first_name, ' ', COALESCE(u.middle_name, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.rating >= :minRating) " +
           "AND (:genderId IS NULL OR u.gender_id = :genderId) " +
           "AND (COALESCE(:maxWorkExperienceDate, '9999-12-31'::date) = '9999-12-31'::date OR d.work_experience <= :maxWorkExperienceDate) " +
           "ORDER BY COALESCE(d.work_experience, '9999-12-31'::date) ASC",
           countQuery = "SELECT COUNT(*) FROM doctors d " +
           "JOIN users u ON u.id_user = d.user_id " +
           "WHERE (:name IS NULL OR (LOWER(CONCAT(u.last_name, ' ', u.first_name, ' ', COALESCE(u.middle_name, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.rating >= :minRating) " +
           "AND (:genderId IS NULL OR u.gender_id = :genderId) " +
           "AND (COALESCE(:maxWorkExperienceDate, '9999-12-31'::date) = '9999-12-31'::date OR d.work_experience <= :maxWorkExperienceDate)",
           nativeQuery = true)
    Page<Doctor> findByFiltersOrderByExperience(@Param("specialization") String specialization,
                                                 @Param("minRating") Double minRating,
                                                 @Param("name") String name,
                                                 @Param("genderId") Integer genderId,
                                                 @Param("maxWorkExperienceDate") LocalDate maxWorkExperienceDate,
                                                 Pageable pageable);

    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN users u ON u.id_user = d.user_id " +
           "WHERE (:name IS NULL OR (LOWER(CONCAT(u.last_name, ' ', u.first_name, ' ', COALESCE(u.middle_name, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.rating >= :minRating) " +
           "AND (:genderId IS NULL OR u.gender_id = :genderId) " +
           "AND (COALESCE(:maxWorkExperienceDate, '9999-12-31'::date) = '9999-12-31'::date OR d.work_experience <= :maxWorkExperienceDate) " +
           "ORDER BY d.rating DESC NULLS LAST",
           countQuery = "SELECT COUNT(*) FROM doctors d " +
           "JOIN users u ON u.id_user = d.user_id " +
           "WHERE (:name IS NULL OR (LOWER(CONCAT(u.last_name, ' ', u.first_name, ' ', COALESCE(u.middle_name, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')))) " +
           "AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) " +
           "AND (:minRating IS NULL OR d.rating >= :minRating) " +
           "AND (:genderId IS NULL OR u.gender_id = :genderId) " +
           "AND (COALESCE(:maxWorkExperienceDate, '9999-12-31'::date) = '9999-12-31'::date OR d.work_experience <= :maxWorkExperienceDate)",
           nativeQuery = true)
    Page<Doctor> findByFiltersOrderByRating(@Param("specialization") String specialization,
                                             @Param("minRating") Double minRating,
                                             @Param("name") String name,
                                             @Param("genderId") Integer genderId,
                                             @Param("maxWorkExperienceDate") LocalDate maxWorkExperienceDate,
                                             Pageable pageable);

    List<Doctor> findTop10ByOrderByRatingDesc();
}
