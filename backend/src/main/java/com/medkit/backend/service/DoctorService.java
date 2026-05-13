package com.medkit.backend.service;

import com.medkit.backend.dto.request.UpdateDoctorRequest;
import com.medkit.backend.dto.response.DoctorResponse;
import com.medkit.backend.dto.response.DoctorStatsResponse;
import com.medkit.backend.entity.AppointmentSlot;
import com.medkit.backend.entity.Doctor;
import com.medkit.backend.entity.User;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.AppointmentSlotRepository;
import com.medkit.backend.repository.DoctorRepository;
import com.medkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public DoctorResponse getDoctorById(Integer id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден с ID: " + id));
        return mapToResponse(doctor);
    }

    public Page<DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = false)
    public Page<DoctorResponse> searchDoctors(String specialization, Double minRating, String name,
                                               String gender, Integer minExperience, Pageable pageable) {
        LocalDate maxWorkExperienceDate = null;
        if (minExperience != null && minExperience > 0) {
            maxWorkExperienceDate = LocalDate.now().minusYears(minExperience);
        }

        // Convert gender string to ID
        Integer genderId = null;
        if (gender != null && !gender.isEmpty()) {
            if ("male".equalsIgnoreCase(gender)) {
                genderId = 1;
            } else if ("female".equalsIgnoreCase(gender)) {
                genderId = 2;
            }
        }

        // Determine sort type from pageable
        String sortType = "rating"; // default
        if (pageable.getSort().isSorted()) {
            for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();
                if ("next_available_slot_date".equals(property) || "next_available_slot_time".equals(property)) {
                    sortType = "slot";
                    break;
                } else if ("work_experience".equals(property)) {
                    sortType = "experience";
                    break;
                } else if ("rating".equals(property)) {
                    sortType = "rating";
                    break;
                }
            }
        }

        // If sorting by slot, update cache for all doctors first
        if ("slot".equals(sortType)) {
            updateNextAvailableSlotCache();
        }

        // Create unsorted pageable to prevent Spring from adding ORDER BY
        Pageable unsortedPageable = org.springframework.data.domain.PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        // Call appropriate repository method based on sort type
        Page<Doctor> doctors;
        switch (sortType) {
            case "slot":
                doctors = doctorRepository.findByFiltersOrderBySlot(
                    specialization, minRating, name, genderId, maxWorkExperienceDate, unsortedPageable);
                break;
            case "experience":
                doctors = doctorRepository.findByFiltersOrderByExperience(
                    specialization, minRating, name, genderId, maxWorkExperienceDate, unsortedPageable);
                break;
            case "rating":
            default:
                doctors = doctorRepository.findByFiltersOrderByRating(
                    specialization, minRating, name, genderId, maxWorkExperienceDate, unsortedPageable);
                break;
        }

        return doctors.map(this::mapToResponse);
    }

    @Transactional(readOnly = false)
    private void updateNextAvailableSlotCache() {
        List<Doctor> allDoctors = doctorRepository.findAll();
        for (Doctor doctor : allDoctors) {
            calculateAndCacheNextAvailableSlot(doctor);
        }
        // Force flush to ensure changes are persisted
        doctorRepository.flush();
    }

    public List<DoctorResponse> getTopRatedDoctors() {
        return doctorRepository.findTop10ByOrderByRatingDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DoctorStatsResponse getDoctorStats(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден с ID: " + doctorId));

        LocalDate weekAgo = LocalDate.now().minusWeeks(1);
        LocalDate today = LocalDate.now();

        List<AppointmentSlot> weekSlots = appointmentSlotRepository
                .findByDoctorAndDateRange(doctorId, weekAgo, today);

        List<AppointmentSlot> todaySlots = appointmentSlotRepository
                .findByDoctorAndDateRange(doctorId, today, today);

        int todayCompletedAppointments = (int) todaySlots.stream()
                .filter(slot -> slot.getStatus() != null &&
                        "completed".equalsIgnoreCase(slot.getStatus().getTitle()))
                .count();

        int weekCompletedAppointments = (int) weekSlots.stream()
                .filter(slot -> slot.getStatus() != null &&
                        "completed".equalsIgnoreCase(slot.getStatus().getTitle()))
                .count();

        return new DoctorStatsResponse(
                todayCompletedAppointments,
                weekCompletedAppointments
        );
    }

    @Transactional
    public DoctorResponse updateDoctor(Integer doctorId, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден с ID: " + doctorId));

        User user = doctor.getUser();

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

        userRepository.save(user);

        return mapToResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateNotificationSettings(Integer doctorId, Boolean notifyCancellations, Boolean notifyBookings) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден с ID: " + doctorId));

        if (notifyCancellations != null) {
            doctor.setNotifyCancellations(notifyCancellations);
        }
        if (notifyBookings != null) {
            doctor.setNotifyBookings(notifyBookings);
        }
        doctorRepository.save(doctor);

        return mapToResponse(doctor);
    }

    @Transactional
    public Map<String, String> uploadAvatar(Integer doctorId, MultipartFile file) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден с ID: " + doctorId));

        User user = doctor.getUser();

        // Удаляем старый аватар если есть
        if (user.getAvatarUrl() != null) {
            fileStorageService.deleteFile(user.getAvatarUrl());
        }

        // Сохраняем новый аватар
        String avatarUrl = fileStorageService.storeFile(file, "avatars");
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("avatarUrl", avatarUrl);
        return response;
    }

    @Transactional
    public void deleteAvatar(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден с ID: " + doctorId));

        User user = doctor.getUser();

        if (user.getAvatarUrl() != null) {
            fileStorageService.deleteFile(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setIdDoctor(doctor.getIdDoctor());
        response.setUserId(doctor.getUser().getIdUser());
        response.setEmail(doctor.getUser().getEmail());
        response.setLastName(doctor.getUser().getLastName());
        response.setFirstName(doctor.getUser().getFirstName());
        response.setMiddleName(doctor.getUser().getMiddleName());
        response.setPhoneNumber(doctor.getUser().getPhoneNumber());
        response.setAvatarUrl(doctor.getUser().getAvatarUrl());
        response.setSpecialization(doctor.getSpecialization());
        response.setRating(doctor.getRating());
        response.setReviewsCount(doctor.getReviewsCount());
        response.setOffice(doctor.getOffice());

        // Calculate experience years from start date
        if (doctor.getWorkExperience() != null) {
            Period period = Period.between(doctor.getWorkExperience(), LocalDate.now());
            int years = period.getYears();
            response.setExperienceYears(years);
            response.setWorkExperience(years + " лет");
        } else {
            response.setExperienceYears(0);
            response.setWorkExperience(null);
        }

        // Set gender
        if (doctor.getUser().getGenderId() != null) {
            String genderName = doctor.getUser().getGenderId() == 1 ? "male" : "female";
            response.setGender(genderName);
        } else {
            response.setGender(null);
        }

        // Use cached next available slot or calculate if not cached
        String nextSlot = calculateAndCacheNextAvailableSlot(doctor);
        response.setNextAvailableSlot(nextSlot);

        response.setNotifyCancellations(doctor.getNotifyCancellations());
        response.setNotifyBookings(doctor.getNotifyBookings());
        return response;
    }

    private String calculateAndCacheNextAvailableSlot(Doctor doctor) {
        LocalDate today = LocalDate.now();
        java.time.LocalTime currentTime = java.time.LocalTime.now();
        LocalDate endDate = today.plusDays(90);

        List<com.medkit.backend.entity.AppointmentSlot> availableSlots = appointmentSlotRepository
                .findAvailableSlotsByDoctorAndDateRange(doctor.getIdDoctor(), today, endDate);

        if (availableSlots.isEmpty()) {
            // Clear cached values if no slots available
            if (doctor.getNextAvailableSlotDate() != null || doctor.getNextAvailableSlotTime() != null) {
                doctor.setNextAvailableSlotDate(null);
                doctor.setNextAvailableSlotTime(null);
                doctorRepository.save(doctor);
            }
            return null;
        }

        // Filter slots to only include future slots (after current time if today)
        com.medkit.backend.entity.AppointmentSlot nextSlot = availableSlots.stream()
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
            // No future slots available
            if (doctor.getNextAvailableSlotDate() != null || doctor.getNextAvailableSlotTime() != null) {
                doctor.setNextAvailableSlotDate(null);
                doctor.setNextAvailableSlotTime(null);
                doctorRepository.save(doctor);
            }
            return null;
        }

        // Update cached values in database for sorting
        boolean needsUpdate = doctor.getNextAvailableSlotDate() == null ||
                              doctor.getNextAvailableSlotTime() == null ||
                              !nextSlot.getSlotDate().equals(doctor.getNextAvailableSlotDate()) ||
                              !nextSlot.getStartTime().equals(doctor.getNextAvailableSlotTime());

        if (needsUpdate) {
            doctor.setNextAvailableSlotDate(nextSlot.getSlotDate());
            doctor.setNextAvailableSlotTime(nextSlot.getStartTime());
            doctorRepository.save(doctor);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM", java.util.Locale.forLanguageTag("ru"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateStr = nextSlot.getSlotDate().format(dateFormatter);
        String timeStr = nextSlot.getStartTime().format(timeFormatter);

        return dateStr + " в " + timeStr;
    }
}
