package com.medkit.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String email, String code) {
        log.info("Attempting to send verification code to: {}", email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Код подтверждения MedKit");
            message.setText("Здравствуйте!\n\n" +
                    "Ваш код подтверждения для регистрации в MedKit: " + code + "\n\n" +
                    "Код действителен в течение 10 минут.\n\n" +
                    "Если вы не регистрировались в MedKit, проигнорируйте это письмо.");

            log.info("Sending email via JavaMailSender...");
            mailSender.send(message);
            log.info("Verification code sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", email, e);
            log.error("Error details: {}", e.getMessage());
            // Fallback: логируем в консоль для разработки
            log.info("=== EMAIL VERIFICATION CODE ===");
            log.info("To: {}", email);
            log.info("Code: {}", code);
            log.info("===============================");
        }
    }

    public void sendPasswordResetCode(String email, String code) {
        log.info("Attempting to send password reset code to: {}", email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Код для сброса пароля MedKit");
            message.setText("Здравствуйте!\n\n" +
                    "Вы запросили сброс пароля для вашего аккаунта MedKit.\n\n" +
                    "Ваш код подтверждения: " + code + "\n\n" +
                    "Код действителен в течение 10 минут.\n\n" +
                    "Если вы не запрашивали сброс пароля, проигнорируйте это письмо.");

            log.info("Sending email via JavaMailSender...");
            mailSender.send(message);
            log.info("Password reset code sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset code to: {}", email, e);
            log.error("Error details: {}", e.getMessage());
            // Fallback: логируем в консоль для разработки
            log.info("=== PASSWORD RESET CODE ===");
            log.info("To: {}", email);
            log.info("Code: {}", code);
            log.info("===========================");
        }
    }
}
