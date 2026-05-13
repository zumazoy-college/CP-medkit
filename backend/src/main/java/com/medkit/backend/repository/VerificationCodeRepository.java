package com.medkit.backend.repository;

import com.medkit.backend.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {

    Optional<VerificationCode> findByEmailAndCodeAndIsUsedFalseAndExpiresAtAfter(
            String email, String code, LocalDateTime now);

    Optional<VerificationCode> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}
