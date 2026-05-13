package com.medkit.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Scanner;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Введите пароль: ");
        String password = sc.nextLine().strip();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt hash: " + hash);
    }
}
