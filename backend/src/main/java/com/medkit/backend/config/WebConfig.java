package com.medkit.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Настройка обработки статических файлов аватарок
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:uploads/avatars/");

        // Добавляем путь /uploads/avatars/** для совместимости
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
    }
}
