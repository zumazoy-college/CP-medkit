package com.medkit.backend.service;

import com.medkit.backend.dto.response.FavoriteResponse;
import com.medkit.backend.entity.AppointmentSlot;
import com.medkit.backend.entity.Doctor;
import com.medkit.backend.entity.Favorite;
import com.medkit.backend.entity.Patient;
import com.medkit.backend.entity.User;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.AppointmentSlotRepository;
import com.medkit.backend.repository.DoctorRepository;
import com.medkit.backend.repository.FavoriteRepository;
import com.medkit.backend.repository.PatientRepository;
import com.medkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;

    @Transactional
    public FavoriteResponse toggleFavorite(String userEmail, Integer doctorId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        // Find existing favorite record (including deleted ones)
        Optional<Favorite> existingFavorite = favoriteRepository
                .findByPatient_IdPatientAndDoctor_IdDoctor(patient.getIdPatient(), doctorId);

        if (existingFavorite.isPresent()) {
            Favorite favorite = existingFavorite.get();
            // Toggle is_deleted flag
            favorite.setIsDeleted(!favorite.getIsDeleted());
            favoriteRepository.save(favorite);

            if (favorite.getIsDeleted()) {
                // Removed from favorites
                return null;
            } else {
                // Restored to favorites
                return mapToResponse(favorite);
            }
        } else {
            // Create new favorite
            Favorite favorite = new Favorite();
            favorite.setPatient(patient);
            favorite.setDoctor(doctor);
            favorite.setIsDeleted(false);

            Favorite saved = favoriteRepository.save(favorite);
            return mapToResponse(saved);
        }
    }

    @Transactional
    public void removeFavorite(String userEmail, Integer doctorId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        List<Favorite> favorites = favoriteRepository.findByPatient_IdPatientAndIsDeletedFalse(patient.getIdPatient());
        Favorite favorite = favorites.stream()
                .filter(f -> f.getDoctor().getIdDoctor().equals(doctorId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден в избранном"));

        favorite.setIsDeleted(true);
        favoriteRepository.save(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        return favoriteRepository.findByPatient_IdPatientAndIsDeletedFalse(patient.getIdPatient())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FavoriteResponse mapToResponse(Favorite favorite) {
        FavoriteResponse response = new FavoriteResponse();
        response.setIdFavorite(favorite.getIdFavorite());
        response.setDoctorId(favorite.getDoctor().getIdDoctor());
        response.setDoctorName(favorite.getDoctor().getUser().getLastName() + " " +
                              favorite.getDoctor().getUser().getFirstName());
        response.setSpecialization(favorite.getDoctor().getSpecialization());
        response.setRating(favorite.getDoctor().getRating().doubleValue());
        response.setAvatarUrl(favorite.getDoctor().getUser().getAvatarUrl());

        // Calculate next available slot
        String nextSlot = calculateNextAvailableSlot(favorite.getDoctor());
        response.setNextAvailableSlot(nextSlot);

        response.setCreatedAt(favorite.getCreatedAt());
        return response;
    }

    private String calculateNextAvailableSlot(Doctor doctor) {
        LocalDate today = LocalDate.now();
        java.time.LocalTime currentTime = java.time.LocalTime.now();
        LocalDate endDate = today.plusDays(90);

        List<AppointmentSlot> availableSlots = appointmentSlotRepository
                .findAvailableSlotsByDoctorAndDateRange(doctor.getIdDoctor(), today, endDate);

        if (availableSlots.isEmpty()) {
            return null;
        }

        // Filter slots to only include future slots (after current time if today)
        AppointmentSlot nextSlot = availableSlots.stream()
                .filter(slot -> {
                    if (slot.getSlotDate().isAfter(today)) {
                        return true; // Future dates are always valid
                    } else if (slot.getSlotDate().isEqual(today)) {
                        return slot.getStartTime().isAfter(currentTime); // Today: only future times
                    }
                    return false; // Past dates are not valid
                })
                .findFirst()
                .orElse(null);

        if (nextSlot == null) {
            return null;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM", java.util.Locale.forLanguageTag("ru"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateStr = nextSlot.getSlotDate().format(dateFormatter);
        String timeStr = nextSlot.getStartTime().format(timeFormatter);

        return dateStr + " в " + timeStr;
    }
}
