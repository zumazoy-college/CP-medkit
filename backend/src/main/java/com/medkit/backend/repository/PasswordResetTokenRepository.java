package com.medkit.backend.repository;

import com.medkit.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByTokenAndIsUsedFalseAndExpiresAtAfter(
            String token, LocalDateTime now);

    void deleteByEmail(String email);
}
