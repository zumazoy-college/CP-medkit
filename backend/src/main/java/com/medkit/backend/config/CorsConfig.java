package com.medkit.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешенные origins (фронтенд приложения)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",     // Все localhost порты для разработки
                "http://127.0.0.1:*",     // Альтернативный localhost
                "http://10.0.2.2:*"       // Android эмулятор
        ));

        // Разрешенные HTTP методы
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Разрешенные заголовки
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Разрешить отправку credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Максимальное время кеширования preflight запроса
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
