package com.medkit.backend.service;

import com.medkit.backend.dto.response.FileResponse;
import com.medkit.backend.entity.Appointment;
import com.medkit.backend.entity.FileEntity;
import com.medkit.backend.entity.Patient;
import com.medkit.backend.entity.User;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.exception.UnauthorizedException;
import com.medkit.backend.repository.AppointmentRepository;
import com.medkit.backend.repository.DoctorRepository;
import com.medkit.backend.repository.FileRepository;
import com.medkit.backend.repository.PatientRepository;
import com.medkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public FileResponse uploadFile(String userEmail, Integer appointmentId, MultipartFile file) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        boolean isDoctor = appointment.getSlot().getDoctor().getUser().getIdUser().equals(user.getIdUser());
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isDoctor && !isPatient) {
            throw new UnauthorizedException("Вы не можете загружать файлы к этому приему");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("Файл пустой");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String storedFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            FileEntity fileEntity = new FileEntity();
            fileEntity.setAppointment(appointment);
            fileEntity.setFileName(originalFilename);
            fileEntity.setFileUrl(storedFileName);

            FileEntity saved = fileRepository.save(fileEntity);
            return mapToResponse(saved);

        } catch (IOException e) {
            throw new BadRequestException("Не удалось загрузить файл: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getAppointmentFiles(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        // Проверяем доступ - любой врач или пациент этого приема
        boolean isAnyDoctor = doctorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isAnyDoctor && !isPatient) {
            throw new UnauthorizedException("Вы не можете просматривать файлы этого приема");
        }

        return fileRepository.findByAppointment_IdAppointmentOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getPatientFiles(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        // Get all completed appointments for the patient
        List<Appointment> completedAppointments = appointmentRepository
                .findByPatient_IdPatientAndSlot_Status_TitleOrderByCreatedAtDesc(
                        patient.getIdPatient(), "completed");

        // Get all files from these appointments
        return completedAppointments.stream()
                .flatMap(appointment -> fileRepository
                        .findByAppointment_IdAppointmentOrderByCreatedAtDesc(appointment.getIdAppointment())
                        .stream())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(String userEmail, Integer fileId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не найден"));

        Appointment appointment = fileEntity.getAppointment();

        // Проверяем доступ - любой врач или пациент этого приема
        boolean isAnyDoctor = doctorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isAnyDoctor && !isPatient) {
            throw new UnauthorizedException("Вы не можете скачать этот файл");
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(fileEntity.getFileUrl());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Файл не найден на диске");
            }
        } catch (Exception e) {
            throw new BadRequestException("Не удалось скачать файл: " + e.getMessage());
        }
    }

    private FileResponse mapToResponse(FileEntity fileEntity) {
        FileResponse response = new FileResponse();
        response.setId(fileEntity.getIdFile());
        response.setAppointmentId(fileEntity.getAppointment().getIdAppointment());
        response.setFileName(fileEntity.getFileName());
        response.setUploadedAt(fileEntity.getCreatedAt());
        return response;
    }

    @Transactional
    public void deleteFile(String userEmail, Integer fileId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не найден"));

        Appointment appointment = fileEntity.getAppointment();

        // Only doctor can delete files
        boolean isDoctor = appointment.getSlot().getDoctor().getUser().getIdUser().equals(user.getIdUser());
        if (!isDoctor) {
            throw new UnauthorizedException("Вы не можете удалить этот файл");
        }

        // Check if appointment is cancelled
        if (appointment.getSlot().getCancellationReason() != null) {
            throw new IllegalStateException("Невозможно удалить файл из отмененного приема");
        }

        // Delete physical file
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileEntity.getFileUrl());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        // Delete from database
        fileRepository.delete(fileEntity);
    }
}
