package com.medkit.backend.service;

import com.medkit.backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new BadRequestException("Файл пустой");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BadRequestException("Имя файла отсутствует");
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path uploadPath = Paths.get(uploadDir, subDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Не удалось сохранить файл: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(uploadDir + fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Игнорируем ошибки удаления
        }
    }
}
