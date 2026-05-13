package com.medkit.backend.service;

import com.medkit.backend.dto.request.CreateSlotRequest;
import com.medkit.backend.dto.request.GenerateSlotsRequest;
import com.medkit.backend.dto.response.AppointmentSlotResponse;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotManagementService {

    private final AppointmentSlotRepository slotRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final SlotStatusRepository slotStatusRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentSlotResponse createSlot(String userEmail, CreateSlotRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        if (request.getSlotDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Нельзя создать слот в прошлом");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Время начала должно быть раньше времени окончания");
        }

        boolean exists = slotRepository.findByDoctor_IdDoctorAndSlotDateAndStartTime(
                doctor.getIdDoctor(), request.getSlotDate(), request.getStartTime()
        ).isPresent();

        if (exists) {
            throw new BadRequestException("Слот на это время уже существует");
        }

        SlotStatus freeStatus = slotStatusRepository.findByTitle("free")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'free' не найден"));

        AppointmentSlot slot = new AppointmentSlot();
        slot.setDoctor(doctor);
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus(freeStatus);

        AppointmentSlot saved = slotRepository.save(slot);
        return mapToResponse(saved);
    }

    @Transactional
    public List<AppointmentSlotResponse> generateSlots(String userEmail, GenerateSlotsRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Дата начала не может быть в прошлом");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Дата окончания должна быть после даты начала");
        }

        if (request.getSlotDurationMinutes() < 5 || request.getSlotDurationMinutes() > 120) {
            throw new BadRequestException("Длительность слота должна быть от 5 до 120 минут");
        }

        List<Schedule> schedules = scheduleRepository.findByDoctor_IdDoctorAndIsActiveTrue(doctor.getIdDoctor());
        if (schedules.isEmpty()) {
            throw new BadRequestException("У врача нет активного расписания");
        }

        // Get all existing slots in the date range
        List<AppointmentSlot> existingSlots = slotRepository.findByDoctorAndDateRange(
                doctor.getIdDoctor(), request.getStartDate(), request.getEndDate());

        SlotStatus freeStatus = slotStatusRepository.findByTitle("free")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'free' не найден"));

        // Separate free and booked/completed slots
        List<AppointmentSlot> freeSlots = existingSlots.stream()
                .filter(slot -> "free".equals(slot.getStatus().getTitle()))
                .collect(Collectors.toList());

        List<AppointmentSlot> bookedSlots = existingSlots.stream()
                .filter(slot -> !"free".equals(slot.getStatus().getTitle()))
                .collect(Collectors.toList());

        // Delete only free slots to regenerate them
        if (!freeSlots.isEmpty()) {
            slotRepository.deleteAll(freeSlots);
            slotRepository.flush(); // Ensure deletion is committed before creating new slots
        }

        List<ScheduleException> exceptions = scheduleExceptionRepository.findByDoctorAndDateRange(
                doctor.getIdDoctor(), request.getStartDate(), request.getEndDate()
        );

        List<AppointmentSlot> newSlots = new ArrayList<>();

        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            LocalDate finalCurrentDate = currentDate;

            boolean hasException = exceptions.stream()
                    .anyMatch(ex -> !finalCurrentDate.isBefore(ex.getStartDate()) &&
                                   !finalCurrentDate.isAfter(ex.getEndDate()));

            if (!hasException) {
                int dayOfWeek = currentDate.getDayOfWeek().getValue() % 7;

                // Use the new method to find active schedule for this specific date
                Schedule daySchedule = scheduleRepository.findActiveScheduleForDate(
                        doctor.getIdDoctor(), dayOfWeek, currentDate
                ).orElse(null);

                if (daySchedule != null) {
                    LocalTime currentTime = daySchedule.getWorkStart();
                    LocalTime endTime = daySchedule.getWorkEnd();
                    LocalTime lunchStart = daySchedule.getLunchStart();
                    LocalTime lunchEnd = daySchedule.getLunchEnd();

                    while (currentTime.plusMinutes(request.getSlotDurationMinutes()).isBefore(endTime) ||
                           currentTime.plusMinutes(request.getSlotDurationMinutes()).equals(endTime)) {

                        LocalTime slotEnd = currentTime.plusMinutes(request.getSlotDurationMinutes());

                        // Пропускаем слоты, которые попадают на перерыв
                        boolean isDuringLunch = false;
                        if (lunchStart != null && lunchEnd != null) {
                            isDuringLunch = (currentTime.isBefore(lunchEnd) && slotEnd.isAfter(lunchStart));
                        }

                        if (!isDuringLunch) {
                            // Skip slots in the past for today
                            LocalTime finalCurrentTime = currentTime;
                            if (finalCurrentDate.equals(LocalDate.now()) && finalCurrentTime.isBefore(LocalTime.now())) {
                                currentTime = slotEnd;
                                continue;
                            }

                            // Check if slot already exists (booked or completed slots)
                            boolean slotExists = bookedSlots.stream()
                                    .anyMatch(slot -> slot.getSlotDate().equals(finalCurrentDate) &&
                                                     slot.getStartTime().equals(finalCurrentTime));

                            if (!slotExists) {
                                AppointmentSlot slot = new AppointmentSlot();
                                slot.setDoctor(doctor);
                                slot.setSlotDate(currentDate);
                                slot.setStartTime(currentTime);
                                slot.setEndTime(slotEnd);
                                slot.setStatus(freeStatus);
                                newSlots.add(slot);
                            }
                        }

                        currentTime = slotEnd;

                        // Если следующий слот начинается во время перерыва, пропускаем до конца перерыва
                        if (lunchStart != null && lunchEnd != null &&
                            currentTime.isAfter(lunchStart) && currentTime.isBefore(lunchEnd)) {
                            currentTime = lunchEnd;
                        }
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        // Save slots in batches to avoid memory issues
        List<AppointmentSlot> savedSlots = new ArrayList<>();
        int batchSize = 500;
        for (int i = 0; i < newSlots.size(); i += batchSize) {
            int end = Math.min(i + batchSize, newSlots.size());
            List<AppointmentSlot> batch = newSlots.subList(i, end);
            savedSlots.addAll(slotRepository.saveAll(batch));
            slotRepository.flush();
        }

        return savedSlots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(String userEmail, Integer slotId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Слот не найден"));

        if (!slot.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new BadRequestException("Вы не можете удалить чужой слот");
        }

        if (!"free".equals(slot.getStatus().getTitle())) {
            throw new BadRequestException("Нельзя удалить занятый слот");
        }

        slotRepository.delete(slot);
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> getDoctorSlotsInRange(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("Дата окончания должна быть после даты начала");
        }

        List<AppointmentSlot> slots = slotRepository.findByDoctor_IdDoctorAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                doctor.getIdDoctor(), startDate, endDate);

        return slots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AppointmentSlotResponse mapToResponse(AppointmentSlot slot) {
        AppointmentSlotResponse response = new AppointmentSlotResponse();
        response.setIdSlot(slot.getIdSlot());
        response.setDoctorId(slot.getDoctor().getIdDoctor());
        response.setDoctorName(slot.getDoctor().getUser().getLastName() + " " +
                               slot.getDoctor().getUser().getFirstName());

        if (slot.getPatient() != null) {
            response.setPatientId(slot.getPatient().getIdPatient());
            response.setPatientName(slot.getPatient().getUser().getLastName() + " " +
                                   slot.getPatient().getUser().getFirstName() + " " +
                                   (slot.getPatient().getUser().getMiddleName() != null ?
                                    slot.getPatient().getUser().getMiddleName() : ""));
        }

        if (slot.getAppointment() != null) {
            response.setAppointmentId(slot.getAppointment().getIdAppointment());
        }

        response.setSlotDate(slot.getSlotDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setStatus(slot.getStatus().getTitle());
        response.setCancellationReason(slot.getCancellationReason());
        response.setCreatedAt(slot.getCreatedAt());
        return response;
    }
}
