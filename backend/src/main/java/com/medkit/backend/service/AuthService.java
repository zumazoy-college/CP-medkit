package com.medkit.backend.service;

import com.medkit.backend.dto.request.ChangePasswordRequest;
import com.medkit.backend.dto.request.LoginRequest;
import com.medkit.backend.dto.request.RegisterPatientRequest;
import com.medkit.backend.dto.response.AuthResponse;
import com.medkit.backend.entity.Gender;
import com.medkit.backend.entity.Patient;
import com.medkit.backend.entity.PasswordResetToken;
import com.medkit.backend.entity.User;
import com.medkit.backend.entity.VerificationCode;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.GenderRepository;
import com.medkit.backend.repository.PasswordResetTokenRepository;
import com.medkit.backend.repository.PatientRepository;
import com.medkit.backend.repository.UserRepository;
import com.medkit.backend.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final GenderRepository genderRepository;

    @Transactional
    public AuthResponse registerPatient(RegisterPatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Пользователь с таким email уже существует");
        }

        if (patientRepository.existsBySnils(request.getSnils())) {
            throw new BadRequestException("Пациент с таким СНИЛС уже зарегистрирован");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(User.UserRole.patient);

        // Set gender ID
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            if ("male".equalsIgnoreCase(request.getGender())) {
                user.setGenderId(1);
            } else if ("female".equalsIgnoreCase(request.getGender())) {
                user.setGenderId(2);
            }
        }

        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setBirthdate(request.getBirthdate());
        patient.setSnils(request.getSnils());
        patient.setAllergies(request.getAllergies());
        patient.setChronicDiseases(request.getChronicDiseases());

        patientRepository.save(patient);

        String token = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        savedUser.setRefreshToken(refreshToken);
        userRepository.save(savedUser);

        return new AuthResponse(
                token,
                refreshToken,
                savedUser.getIdUser(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Аккаунт деактивирован");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Неверный пароль");
        }

        // Проверка роли, если указана expectedRole
        if (request.getExpectedRole() != null && !request.getExpectedRole().isEmpty()) {
            if (!user.getRole().name().equalsIgnoreCase(request.getExpectedRole())) {
                throw new BadRequestException("Доступ запрещен. Неверная роль пользователя.");
            }
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Если это врач, добавляем doctorId
        if (user.getRole() == User.UserRole.doctor) {
            Integer doctorId = userRepository.findById(user.getIdUser())
                    .flatMap(u -> u.getDoctor() != null ?
                            java.util.Optional.of(u.getDoctor().getIdDoctor()) :
                            java.util.Optional.empty())
                    .orElse(null);

            return new AuthResponse(
                    token,
                    refreshToken,
                    user.getIdUser(),
                    doctorId,
                    user.getEmail(),
                    user.getRole().name(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getMiddleName()
            );
        }

        return new AuthResponse(
                token,
                refreshToken,
                user.getIdUser(),
                user.getEmail(),
                user.getRole().name(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getOld_password(), user.getPasswordHash())) {
            throw new BadRequestException("Неверный текущий пароль");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNew_password()));
        userRepository.save(user);
    }

    @Transactional
    public void sendVerificationCode(String email) {
        // Проверяем, что email еще не зарегистрирован
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Пользователь с таким email уже существует");
        }

        // Удаляем старые коды для этого email
        verificationCodeRepository.deleteByEmail(email);

        // Генерируем 6-значный код
        String code = String.format("%06d", new Random().nextInt(999999));

        // Создаем запись с кодом (действителен 10 минут)
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCode.setIsUsed(false);

        verificationCodeRepository.save(verificationCode);

        // Отправляем код на email
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCodeAndIsUsedFalseAndExpiresAtAfter(email, code, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Неверный или истекший код верификации"));

        // Помечаем код как использованный
        verificationCode.setIsUsed(true);
        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public void sendPasswordResetCode(String email) {
        // Проверяем, что пользователь существует
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким email не найден"));

        // Удаляем старые коды для этого email
        verificationCodeRepository.deleteByEmail(email);

        // Генерируем 6-значный код
        String code = String.format("%06d", new Random().nextInt(999999));

        // Создаем запись с кодом (действителен 10 минут)
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCode.setIsUsed(false);

        verificationCodeRepository.save(verificationCode);

        // Отправляем код на email
        emailService.sendPasswordResetCode(email, code);
    }

    @Transactional(readOnly = true)
    public void verifyPasswordResetCode(String email, String code) {
        // Проверяем код
        verificationCodeRepository
                .findByEmailAndCodeAndIsUsedFalseAndExpiresAtAfter(email, code, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Неверный или истекший код подтверждения"));
    }

    @Transactional
    public void verifyCodeAndResetPassword(String email, String code, String newPassword) {
        // Проверяем код
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCodeAndIsUsedFalseAndExpiresAtAfter(email, code, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Неверный или истекший код подтверждения"));

        // Находим пользователя
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Обновляем пароль
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Помечаем код как использованный
        verificationCode.setIsUsed(true);
        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String email = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("Пользователь не найден"));

            if (!jwtService.validateRefreshToken(refreshToken, user)) {
                throw new BadRequestException("Недействительный refresh token");
            }

            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);

            // Если это врач, добавляем doctorId
            if (user.getRole() == User.UserRole.doctor) {
                Integer doctorId = userRepository.findById(user.getIdUser())
                        .flatMap(u -> u.getDoctor() != null ?
                                java.util.Optional.of(u.getDoctor().getIdDoctor()) :
                                java.util.Optional.empty())
                        .orElse(null);

                return new AuthResponse(
                        newAccessToken,
                        newRefreshToken,
                        user.getIdUser(),
                        doctorId,
                        user.getEmail(),
                        user.getRole().name(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getMiddleName()
                );
            }

            return new AuthResponse(
                    newAccessToken,
                    newRefreshToken,
                    user.getIdUser(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getFirstName(),
                    user.getLastName()
            );
        } catch (Exception e) {
            throw new BadRequestException("Не удалось обновить токен");
        }
    }
}
