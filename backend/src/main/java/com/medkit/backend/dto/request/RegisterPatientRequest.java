package com.medkit.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterPatientRequest {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Пароль должен содержать заглавную букву, строчную букву и цифру")
    private String password;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String middleName;

    @Pattern(regexp = "^\\d{11}$", message = "Номер телефона должен содержать 11 цифр")
    private String phoneNumber;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthdate;

    @NotBlank(message = "Пол обязателен")
    @Pattern(regexp = "^(male|female)$", message = "Пол должен быть male или female")
    private String gender;

    @NotBlank(message = "СНИЛС обязателен")
    @Pattern(regexp = "^\\d{11}$", message = "СНИЛС должен содержать 11 цифр")
    private String snils;

    private String allergies;

    private String chronicDiseases;
}
