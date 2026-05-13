package com.medkit.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Integer userId;
    private Integer doctorId;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String middleName;

    public AuthResponse(String token, String refreshToken, Integer userId, String email, String role, String firstName, String lastName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public AuthResponse(String token, String refreshToken, Integer userId, Integer doctorId, String email, String role, String firstName, String lastName, String middleName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.doctorId = doctorId;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
    }
}
