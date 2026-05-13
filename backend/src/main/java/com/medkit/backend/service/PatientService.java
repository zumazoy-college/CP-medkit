package com.medkit.backend.service;

import com.medkit.backend.dto.request.UpdatePatientRequest;
import com.medkit.backend.dto.request.UpdateUserSettingsRequest;
import com.medkit.backend.dto.response.PatientHistoryResponse;
import com.medkit.backend.dto.response.PatientResponse;
import com.medkit.backend.dto.response.UserSettingsResponse;
import com.medkit.backend.entity.Appointment;
import com.medkit.backend.entity.Patient;
import com.medkit.backend.entity.User;
import com.medkit.backend.entity.UserSettings;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.AppointmentRepository;
import com.medkit.backend.repository.PatientRepository;
import com.medkit.backend.repository.UserRepository;
import com.medkit.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserSettingsRepository userSettingsRepository;

    public PatientResponse getPatientById(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден с ID: " + id));
        return mapToResponse(patient);
    }

    public PatientResponse getPatientByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));
        return mapToResponse(patient);
    }

    public PatientResponse getPatientBySnils(String snils) {
        Patient patient = patientRepository.findBySnils(snils)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден с СНИЛС: " + snils));
        return mapToResponse(patient);
    }

    public List<PatientResponse> searchPatients(String query) {
        List<Patient> patients = patientRepository.searchPatients(query);
        return patients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PatientResponse> getDoctorPatients(Integer doctorId) {
        List<Patient> patients = patientRepository.findByDoctorId(doctorId);
        return patients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PatientHistoryResponse> getPatientHistory(Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден с ID: " + patientId));

        // Получаем только завершенные приемы (исключаем отмененные и будущие)
        List<Appointment> appointments = appointmentRepository
                .findByPatient_IdPatientAndSlot_Status_TitleOrderByCreatedAtDesc(patientId, "completed");

        return appointments.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientResponse updateMyProfile(String userEmail, UpdatePatientRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        // Update user fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getMiddleName() != null) {
            user.setMiddleName(request.getMiddleName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update patient fields
        if (request.getAllergies() != null) {
            patient.setAllergies(request.getAllergies());
        }
        if (request.getChronicDiseases() != null) {
            patient.setChronicDiseases(request.getChronicDiseases());
        }

        userRepository.save(user);
        patientRepository.save(patient);

        return mapToResponse(patient);
    }

    private PatientResponse mapToResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setIdPatient(patient.getIdPatient());
        response.setId(patient.getIdPatient()); // Alias
        response.setUserId(patient.getUser().getIdUser());
        response.setEmail(patient.getUser().getEmail());
        response.setLastName(patient.getUser().getLastName());
        response.setFirstName(patient.getUser().getFirstName());
        response.setMiddleName(patient.getUser().getMiddleName());
        response.setPhoneNumber(patient.getUser().getPhoneNumber());
        response.setPhone(patient.getUser().getPhoneNumber()); // Alias
        response.setAvatarUrl(patient.getUser().getAvatarUrl());
        response.setBirthdate(patient.getBirthdate());
        response.setDateOfBirth(patient.getBirthdate()); // Alias
        if (patient.getUser().getGenderId() != null) {
            String genderName = patient.getUser().getGenderId() == 1 ? "male" : "female";
            response.setGender(genderName);
        } else {
            response.setGender(null);
        }
        response.setSnils(patient.getSnils());
        response.setAllergies(patient.getAllergies());
        response.setChronicDiseases(patient.getChronicDiseases());

        // Get last appointment date
        List<Appointment> appointments = appointmentRepository
                .findByPatient_IdPatientOrderByCreatedAtDesc(patient.getIdPatient(), PageRequest.of(0, 1))
                .getContent();
        if (!appointments.isEmpty()) {
            response.setLastAppointmentDate(appointments.get(0).getSlot().getSlotDate());
        }

        return response;
    }

    private PatientHistoryResponse mapToHistoryResponse(Appointment appointment) {
        PatientHistoryResponse response = new PatientHistoryResponse();
        response.setId(appointment.getIdAppointment());
        response.setPatientId(appointment.getPatient().getIdPatient());
        response.setAppointmentId(appointment.getIdAppointment());

        // Combine date and time from slot
        LocalDateTime dateTime = LocalDateTime.of(
            appointment.getSlot().getSlotDate(),
            appointment.getSlot().getStartTime()
        );
        response.setDate(dateTime);

        // Collect diagnoses
        if (appointment.getDiagnoses() != null && !appointment.getDiagnoses().isEmpty()) {
            String diagnosisText = appointment.getDiagnoses().stream()
                    .map(d -> d.getDiagnosis().getIcdCode() + " - " + d.getDiagnosis().getIcdName())
                    .collect(Collectors.joining(", "));
            response.setDiagnosis(diagnosisText);
        }

        // Collect prescriptions
        StringBuilder prescriptions = new StringBuilder();
        if (appointment.getMedicationPrescriptions() != null && !appointment.getMedicationPrescriptions().isEmpty()) {
            prescriptions.append("Лекарства: ");
            prescriptions.append(appointment.getMedicationPrescriptions().stream()
                    .map(m -> m.getMedication().getTitle())
                    .collect(Collectors.joining(", ")));
        }
        if (appointment.getProcedurePrescriptions() != null && !appointment.getProcedurePrescriptions().isEmpty()) {
            if (prescriptions.length() > 0) prescriptions.append("; ");
            prescriptions.append("Процедуры: ");
            prescriptions.append(appointment.getProcedurePrescriptions().stream()
                    .map(p -> p.getProcedure().getTitle())
                    .collect(Collectors.joining(", ")));
        }
        response.setPrescriptions(prescriptions.toString());

        response.setNotes(appointment.getRecommendations());

        // Doctor info
        PatientHistoryResponse.DoctorInfo doctorInfo = new PatientHistoryResponse.DoctorInfo();
        doctorInfo.setId(appointment.getSlot().getDoctor().getIdDoctor());
        doctorInfo.setFirstName(appointment.getSlot().getDoctor().getUser().getFirstName());
        doctorInfo.setLastName(appointment.getSlot().getDoctor().getUser().getLastName());
        doctorInfo.setMiddleName(appointment.getSlot().getDoctor().getUser().getMiddleName());
        doctorInfo.setSpecialization(appointment.getSlot().getDoctor().getSpecialization());
        response.setDoctor(doctorInfo);

        return response;
    }

    // User Settings methods
    public UserSettingsResponse getMySettings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        UserSettings settings = userSettingsRepository.findByUser_IdUser(user.getIdUser())
                .orElseGet(() -> {
                    // Create default settings if not exists
                    UserSettings newSettings = new UserSettings();
                    newSettings.setUser(user);
                    newSettings.setNotifyAppointmentReminder(true);
                    newSettings.setNotifyRatingReminder(true);
                    newSettings.setNotifyAppointmentCancelled(true);
                    newSettings.setDefaultScreen("search");
                    return userSettingsRepository.save(newSettings);
                });

        return mapSettingsToResponse(settings);
    }

    @Transactional
    public UserSettingsResponse updateMySettings(String userEmail, UpdateUserSettingsRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        log.info("Updating settings for user {}: appointmentReminder={}, ratingReminder={}, appointmentCancelled={}, defaultScreen={}",
                user.getIdUser(),
                request.getNotifyAppointmentReminder(),
                request.getNotifyRatingReminder(),
                request.getNotifyAppointmentCancelled(),
                request.getDefaultScreen());

        UserSettings settings = userSettingsRepository.findByUser_IdUser(user.getIdUser())
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings();
                    newSettings.setUser(user);
                    return newSettings;
                });

        settings.setNotifyAppointmentReminder(request.getNotifyAppointmentReminder());
        settings.setNotifyRatingReminder(request.getNotifyRatingReminder());
        settings.setNotifyAppointmentCancelled(request.getNotifyAppointmentCancelled());
        settings.setDefaultScreen(request.getDefaultScreen());

        UserSettings savedSettings = userSettingsRepository.save(settings);

        log.info("Saved settings for user {}: appointmentReminder={}, ratingReminder={}, appointmentCancelled={}, defaultScreen={}",
                user.getIdUser(),
                savedSettings.getNotifyAppointmentReminder(),
                savedSettings.getNotifyRatingReminder(),
                savedSettings.getNotifyAppointmentCancelled(),
                savedSettings.getDefaultScreen());

        return mapSettingsToResponse(savedSettings);
    }

    @Transactional
    public PatientResponse uploadAvatar(String userEmail, MultipartFile file) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        try {
            // Delete old avatar if exists
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                deleteAvatarFile(user.getAvatarUrl());
            }

            // Save new avatar
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/avatars");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/avatars/" + fileName;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return mapToResponse(patient);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    @Transactional
    public PatientResponse deleteAvatar(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            deleteAvatarFile(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }

        return mapToResponse(patient);
    }

    private void deleteAvatarFile(String avatarUrl) {
        try {
            if (avatarUrl.startsWith("/uploads/")) {
                Path filePath = Paths.get(avatarUrl.substring(1)); // Remove leading slash
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Ошибка при удалении файла аватара: " + e.getMessage());
        }
    }

    private UserSettingsResponse mapSettingsToResponse(UserSettings settings) {
        UserSettingsResponse response = new UserSettingsResponse();
        response.setNotifyAppointmentReminder(settings.getNotifyAppointmentReminder());
        response.setNotifyRatingReminder(settings.getNotifyRatingReminder());
        response.setNotifyAppointmentCancelled(settings.getNotifyAppointmentCancelled());
        response.setDefaultScreen(settings.getDefaultScreen());
        return response;
    }
}
