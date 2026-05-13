package com.medkit.backend.controller;

import com.medkit.backend.dto.request.*;
import com.medkit.backend.dto.response.AuthResponse;
import com.medkit.backend.service.AuthService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-verification-code")
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        authService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterPatientRequest request) {
        AuthResponse response = authService.registerPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        authService.changePassword(userEmail, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendPasswordResetCode(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Void> verifyResetCode(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyPasswordResetCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.verifyCodeAndResetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
