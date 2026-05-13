package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Текущий пароль обязателен")
    private String old_password;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Пароль должен содержать заглавную букву, строчную букву и цифру")
    private String new_password;
}
