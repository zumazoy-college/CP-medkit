package com.medkit.backend.service;

import com.medkit.backend.dto.request.CreateScheduleExceptionRequest;
import com.medkit.backend.dto.request.CreateScheduleRequest;
import com.medkit.backend.dto.request.GenerateSlotsRequest;
import com.medkit.backend.dto.response.ScheduleExceptionResponse;
import com.medkit.backend.dto.response.ScheduleResponse;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SlotManagementService slotManagementService;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final SlotStatusRepository slotStatusRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getDoctorSchedule(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return scheduleRepository.findByDoctor_IdDoctorAndIsActiveTrue(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedule(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return scheduleRepository.findByDoctor_IdDoctorAndIsActiveTrue(doctor.getIdDoctor())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse createSchedule(String userEmail, CreateScheduleRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        // Validate times
        validateScheduleTimes(request);

        // Validate date range
        LocalDate effectiveFrom = request.getEffectiveFrom() != null ? request.getEffectiveFrom() : LocalDate.now();
        LocalDate effectiveTo = request.getEffectiveTo();

        if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw new BadRequestException("Дата окончания действия расписания должна быть позже даты начала");
        }

        // Handle overlapping schedules
        handleOverlappingSchedules(doctor.getIdDoctor(), request.getDayOfWeek(), effectiveFrom, effectiveTo);

        // Check for conflicts with existing appointments
        checkAppointmentConflicts(doctor.getIdDoctor(), request, effectiveFrom, effectiveTo);

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setWorkStart(request.getStartTime());
        schedule.setWorkEnd(request.getEndTime());
        schedule.setLunchStart(request.getLunchStart());
        schedule.setLunchEnd(request.getLunchEnd());
        schedule.setAppointmentDuration(request.getAppointmentDuration());
        schedule.setEffectiveFrom(effectiveFrom);
        schedule.setEffectiveTo(effectiveTo);
        schedule.setIsActive(true);

        Schedule saved = scheduleRepository.save(schedule);

        // Auto-generate slots
        generateSlotsForSchedule(userEmail, saved);

        return mapToResponse(saved);
    }

    @Transactional
    public ScheduleResponse updateSchedule(String userEmail, Integer scheduleId, CreateScheduleRequest request) {
        return updateSchedule(userEmail, scheduleId, request, true, true);
    }

    @Transactional
    public ScheduleResponse updateSchedule(String userEmail, Integer scheduleId, CreateScheduleRequest request, boolean generateSlots, boolean checkConflicts) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Расписание не найдено"));

        if (!schedule.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new BadRequestException("Вы не можете изменить чужое расписание");
        }

        // Validate times
        validateScheduleTimes(request);

        // Validate date range
        LocalDate effectiveFrom = request.getEffectiveFrom() != null ? request.getEffectiveFrom() : schedule.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();

        if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw new BadRequestException("Дата окончания действия расписания должна быть позже даты начала");
        }

        // Check for conflicts with existing appointments only if requested
        if (checkConflicts) {
            checkAppointmentConflicts(doctor.getIdDoctor(), request, effectiveFrom, effectiveTo);
        }

        // Update the schedule first
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setWorkStart(request.getStartTime());
        schedule.setWorkEnd(request.getEndTime());
        schedule.setLunchStart(request.getLunchStart());
        schedule.setLunchEnd(request.getLunchEnd());
        schedule.setAppointmentDuration(request.getAppointmentDuration());
        schedule.setEffectiveFrom(effectiveFrom);
        schedule.setEffectiveTo(effectiveTo);

        Schedule updated = scheduleRepository.save(schedule);

        // Handle overlapping schedules AFTER updating current schedule (excluding current schedule)
        handleOverlappingSchedules(doctor.getIdDoctor(), request.getDayOfWeek(), effectiveFrom, effectiveTo, scheduleId);

        // Regenerate slots only if requested
        if (generateSlots) {
            generateSlotsForSchedule(userEmail, updated);
        }

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSchedule(String userEmail, Integer scheduleId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Расписание не найдено"));

        if (!schedule.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new BadRequestException("Вы не можете удалить чужое расписание");
        }

        schedule.setIsActive(false);
        scheduleRepository.save(schedule);
    }

    /**
     * Batch update schedules for a date range
     * This method handles the complete replacement of schedules within a date range
     */
    @Transactional
    public List<ScheduleResponse> batchUpdateSchedules(String userEmail,
                                                       com.medkit.backend.dto.request.BatchUpdateScheduleRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();

        // Validate date range
        if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw new BadRequestException("Дата окончания действия расписания должна быть позже даты начала");
        }

        // Get all days being configured
        List<Integer> configuredDays = request.getSchedules().stream()
                .map(com.medkit.backend.dto.request.CreateScheduleRequest::getDayOfWeek)
                .distinct()
                .collect(Collectors.toList());

        // Find all active schedules for this doctor
        List<Schedule> allActiveSchedules = scheduleRepository.findByDoctor_IdDoctorAndIsActiveTrue(doctor.getIdDoctor());

        // Deactivate or adjust schedules for days NOT in the configured list within the date range
        for (int dayOfWeek = 0; dayOfWeek <= 6; dayOfWeek++) {
            if (!configuredDays.contains(dayOfWeek)) {
                // This day is NOT in the new schedule, so deactivate/adjust old schedules for this day
                handleOverlappingSchedules(doctor.getIdDoctor(), dayOfWeek, effectiveFrom, effectiveTo, null);
            }
        }

        // Create or update schedules for configured days
        List<ScheduleResponse> responses = new ArrayList<>();
        for (com.medkit.backend.dto.request.CreateScheduleRequest scheduleRequest : request.getSchedules()) {
            // Validate times
            validateScheduleTimes(scheduleRequest);

            // Set the date range from batch request
            scheduleRequest.setEffectiveFrom(effectiveFrom);
            scheduleRequest.setEffectiveTo(effectiveTo);

            // Check for conflicts with existing appointments
            checkAppointmentConflicts(doctor.getIdDoctor(), scheduleRequest, effectiveFrom, effectiveTo);

            // Find existing schedule for this day
            Schedule existingSchedule = allActiveSchedules.stream()
                    .filter(s -> s.getDayOfWeek().equals(scheduleRequest.getDayOfWeek()))
                    .findFirst()
                    .orElse(null);

            if (existingSchedule != null) {
                // Update existing schedule without generating slots and without checking conflicts (already checked above)
                ScheduleResponse response = updateSchedule(userEmail, existingSchedule.getIdSchedule(), scheduleRequest, false, false);
                responses.add(response);
            } else {
                // Create new schedule
                ScheduleResponse response = createSchedule(userEmail, scheduleRequest);
                responses.add(response);
            }
        }

        // Generate slots in smaller batches to avoid memory issues
        LocalDate slotStartDate = effectiveFrom.isAfter(LocalDate.now()) ? effectiveFrom : LocalDate.now();
        LocalDate finalEndDate = effectiveTo != null ? effectiveTo : slotStartDate.plusDays(30);

        // Generate slots in 7-day batches
        LocalDate batchStart = slotStartDate;
        while (!batchStart.isAfter(finalEndDate)) {
            LocalDate batchEnd = batchStart.plusDays(6);
            if (batchEnd.isAfter(finalEndDate)) {
                batchEnd = finalEndDate;
            }

            GenerateSlotsRequest generateRequest = new GenerateSlotsRequest();
            generateRequest.setStartDate(batchStart);
            generateRequest.setEndDate(batchEnd);
            generateRequest.setSlotDurationMinutes(request.getSchedules().get(0).getAppointmentDuration());

            try {
                slotManagementService.generateSlots(userEmail, generateRequest);
            } catch (Exception e) {
                // Log error but continue with next batch
            }

            batchStart = batchEnd.plusDays(1);
        }

        return responses;
    }

    @Transactional
    public ScheduleExceptionResponse createScheduleException(String userEmail, CreateScheduleExceptionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Дата окончания должна быть после даты начала");
        }

        // Check for existing appointments in the date range
        List<AppointmentSlot> existingSlots = appointmentSlotRepository
                .findByDoctor_IdDoctorAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        doctor.getIdDoctor(), request.getStartDate(), request.getEndDate());

        long bookedCount = existingSlots.stream()
                .filter(slot -> "booked".equals(slot.getStatus().getTitle()) ||
                               "completed".equals(slot.getStatus().getTitle()))
                .count();

        // If there are appointments and user hasn't confirmed, throw error with count
        if (bookedCount > 0 && !Boolean.TRUE.equals(request.getForceBlock())) {
            throw new BadRequestException("В указанном диапазоне дат есть " + bookedCount +
                    " записей. Все записи будут отменены при блокировке.");
        }

        ScheduleException exception = new ScheduleException();
        exception.setDoctor(doctor);
        exception.setReason(request.getReason());
        exception.setStartDate(request.getStartDate());
        exception.setEndDate(request.getEndDate());

        ScheduleException saved = scheduleExceptionRepository.save(exception);

        // Cancel all slots in the blocked date range
        SlotStatus cancelledStatus = slotStatusRepository.findByTitle("cancelled")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'cancelled' не найден"));

        for (AppointmentSlot slot : existingSlots) {
            if (!"cancelled".equals(slot.getStatus().getTitle())) {
                // Send notification to patient if slot was booked
                if (slot.getPatient() != null) {
                    // Check if patient has notifications enabled
                    UserSettings patientSettings = userSettingsRepository.findByUser_IdUser(slot.getPatient().getUser().getIdUser())
                            .orElse(null);

                    log.info("Patient {} cancellation notification settings (schedule block): {}",
                            slot.getPatient().getIdPatient(),
                            patientSettings != null ? patientSettings.getNotifyAppointmentCancelled() : "null");

                    // Only send notification if patient has this notification type enabled (default true if no settings)
                    boolean shouldNotify = patientSettings == null ||
                                           patientSettings.getNotifyAppointmentCancelled() == null ||
                                           patientSettings.getNotifyAppointmentCancelled();

                    log.info("Should notify patient {} about schedule block cancellation: {}", slot.getPatient().getIdPatient(), shouldNotify);

                    if (shouldNotify) {
                        String doctorName = doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName();
                        String notificationTitle = "Запись отменена";
                        String notificationMessage = String.format(
                                "Врач %s отменил вашу запись на %s в %s. Причина: %s",
                                doctorName,
                                slot.getSlotDate().toString(),
                                slot.getStartTime().toString(),
                                request.getReason() != null ? request.getReason() : "Врач заблокировал дату"
                        );

                        // Find the appointment to get its ID
                        Appointment appointment = appointmentRepository.findBySlot_IdSlot(slot.getIdSlot()).orElse(null);

                        Notification notification = new Notification();
                        notification.setUser(slot.getPatient().getUser());
                        notification.setType("appointment_cancelled");
                        notification.setTitle(notificationTitle);
                        notification.setBody(notificationMessage);
                        notification.setIsRead(false);
                        if (appointment != null) {
                            notification.setLink(String.valueOf(appointment.getIdAppointment()));
                        }
                        notificationRepository.save(notification);
                        log.info("Sent schedule block cancellation notification to patient {}", slot.getPatient().getIdPatient());
                    } else {
                        log.info("Skipped schedule block cancellation notification - patient has notifications disabled");
                    }
                }

                slot.setStatus(cancelledStatus);
                slot.setCancellationReason("Врач заблокировал дату: " +
                        (request.getReason() != null ? request.getReason() : "не указана"));
                appointmentSlotRepository.save(slot);
            }
        }

        return mapExceptionToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleExceptionResponse> getDoctorScheduleExceptions(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return scheduleExceptionRepository.findByDoctor_IdDoctorOrderByStartDateDesc(doctorId)
                .stream()
                .map(this::mapExceptionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleExceptionResponse> getDoctorScheduleExceptionsByDateRange(
            Integer doctorId, LocalDate startDate, LocalDate endDate) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return scheduleExceptionRepository.findByDoctorAndDateRange(doctorId, startDate, endDate)
                .stream()
                .map(this::mapExceptionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleExceptionResponse> getMyScheduleExceptionsByDateRange(
            String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return scheduleExceptionRepository.findByDoctorAndDateRange(doctor.getIdDoctor(), startDate, endDate)
                .stream()
                .map(this::mapExceptionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScheduleException(String userEmail, Integer exceptionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        ScheduleException exception = scheduleExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Исключение расписания не найдено"));

        if (!exception.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new BadRequestException("Вы не можете удалить чужое исключение расписания");
        }

        // Get the date range of the exception
        LocalDate startDate = exception.getStartDate();
        LocalDate endDate = exception.getEndDate();

        // Delete the exception
        scheduleExceptionRepository.delete(exception);

        // Restore cancelled slots in the unblocked date range
        List<AppointmentSlot> cancelledSlots = appointmentSlotRepository
                .findByDoctor_IdDoctorAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        doctor.getIdDoctor(), startDate, endDate);

        SlotStatus freeStatus = slotStatusRepository.findByTitle("free")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'free' не найден"));

        for (AppointmentSlot slot : cancelledSlots) {
            // Only restore slots that were cancelled due to blocking and have no patient
            if ("cancelled".equals(slot.getStatus().getTitle()) &&
                slot.getPatient() == null &&
                slot.getCancellationReason() != null &&
                slot.getCancellationReason().contains("Врач заблокировал дату")) {
                slot.setStatus(freeStatus);
                slot.setCancellationReason(null);
                appointmentSlotRepository.save(slot);
            }
        }
    }

    private ScheduleResponse mapToResponse(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setIdSchedule(schedule.getIdSchedule());
        response.setDoctorId(schedule.getDoctor().getIdDoctor());
        response.setDayOfWeek(schedule.getDayOfWeek());
        response.setStartTime(schedule.getWorkStart());
        response.setEndTime(schedule.getWorkEnd());
        response.setLunchStart(schedule.getLunchStart());
        response.setLunchEnd(schedule.getLunchEnd());
        response.setAppointmentDuration(schedule.getAppointmentDuration());
        response.setEffectiveFrom(schedule.getEffectiveFrom());
        response.setEffectiveTo(schedule.getEffectiveTo());
        response.setIsActive(schedule.getIsActive());
        return response;
    }

    private ScheduleExceptionResponse mapExceptionToResponse(ScheduleException exception) {
        ScheduleExceptionResponse response = new ScheduleExceptionResponse();
        response.setIdException(exception.getIdException());
        response.setDoctorId(exception.getDoctor().getIdDoctor());
        response.setReason(exception.getReason());
        response.setStartDate(exception.getStartDate());
        response.setEndDate(exception.getEndDate());
        return response;
    }

    /**
     * Validate schedule times
     */
    private void validateScheduleTimes(CreateScheduleRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime()) ||
            request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Время начала работы должно быть раньше времени окончания");
        }

        if (request.getLunchStart() != null && request.getLunchEnd() != null) {
            if (request.getLunchStart().isAfter(request.getLunchEnd()) ||
                request.getLunchStart().equals(request.getLunchEnd())) {
                throw new BadRequestException("Время начала перерыва должно быть раньше времени окончания перерыва");
            }
            if (request.getLunchStart().isBefore(request.getStartTime()) ||
                request.getLunchEnd().isAfter(request.getEndTime())) {
                throw new BadRequestException("Время перерыва должно быть в пределах рабочего времени");
            }
        }
    }

    /**
     * Handle overlapping schedules by adjusting their date ranges
     */
    private void handleOverlappingSchedules(Integer doctorId, Integer dayOfWeek,
                                           LocalDate effectiveFrom, LocalDate effectiveTo) {
        handleOverlappingSchedules(doctorId, dayOfWeek, effectiveFrom, effectiveTo, null);
    }

    /**
     * Handle overlapping schedules by adjusting their date ranges
     * @param excludeScheduleId Schedule ID to exclude from overlap check (for updates)
     */
    private void handleOverlappingSchedules(Integer doctorId, Integer dayOfWeek,
                                           LocalDate effectiveFrom, LocalDate effectiveTo,
                                           Integer excludeScheduleId) {
        LocalDate endDate = effectiveTo != null ? effectiveTo : LocalDate.now().plusYears(10);

        List<Schedule> overlapping = scheduleRepository.findOverlappingSchedules(
            doctorId, dayOfWeek, effectiveFrom, endDate);

        for (Schedule oldSchedule : overlapping) {
            // Skip if this is the schedule being updated
            if (excludeScheduleId != null && oldSchedule.getIdSchedule().equals(excludeScheduleId)) {
                continue;
            }

            LocalDate oldStart = oldSchedule.getEffectiveFrom();
            LocalDate oldEnd = oldSchedule.getEffectiveTo();

            // Case 1: Old schedule is completely within new range - deactivate it
            if (!oldStart.isBefore(effectiveFrom) &&
                (oldEnd == null || (effectiveTo == null || !oldEnd.isAfter(effectiveTo)))) {
                oldSchedule.setIsActive(false);
                scheduleRepository.save(oldSchedule);
            }
            // Case 2: Old schedule starts before new range and ends within/after new range start
            else if (oldStart.isBefore(effectiveFrom) &&
                     (oldEnd == null || !oldEnd.isBefore(effectiveFrom))) {
                // Adjust old schedule to end one day before new range starts
                oldSchedule.setEffectiveTo(effectiveFrom.minusDays(1));
                scheduleRepository.save(oldSchedule);
            }
            // Case 3: Old schedule starts within new range and extends beyond
            else if (effectiveTo != null &&
                     !oldStart.isAfter(effectiveTo) &&
                     (oldEnd == null || oldEnd.isAfter(effectiveTo))) {
                // Adjust old schedule to start one day after new range ends
                oldSchedule.setEffectiveFrom(effectiveTo.plusDays(1));
                scheduleRepository.save(oldSchedule);
            }
            // Case 4: Old schedule completely contains new range - split it
            else if (oldStart.isBefore(effectiveFrom) &&
                     effectiveTo != null &&
                     (oldEnd == null || oldEnd.isAfter(effectiveTo))) {
                // Adjust old schedule to end before new range
                oldSchedule.setEffectiveTo(effectiveFrom.minusDays(1));
                scheduleRepository.save(oldSchedule);

                // Create new schedule for the period after new range
                Schedule afterSchedule = new Schedule();
                afterSchedule.setDoctor(oldSchedule.getDoctor());
                afterSchedule.setDayOfWeek(oldSchedule.getDayOfWeek());
                afterSchedule.setWorkStart(oldSchedule.getWorkStart());
                afterSchedule.setWorkEnd(oldSchedule.getWorkEnd());
                afterSchedule.setLunchStart(oldSchedule.getLunchStart());
                afterSchedule.setLunchEnd(oldSchedule.getLunchEnd());
                afterSchedule.setAppointmentDuration(oldSchedule.getAppointmentDuration());
                afterSchedule.setEffectiveFrom(effectiveTo.plusDays(1));
                afterSchedule.setEffectiveTo(oldEnd);
                afterSchedule.setIsActive(true);
                scheduleRepository.save(afterSchedule);
            }
        }
    }

    /**
     * Check for conflicts with existing appointments
     */
    private void checkAppointmentConflicts(Integer doctorId, CreateScheduleRequest request,
                                          LocalDate effectiveFrom, LocalDate effectiveTo) {
        // Only check if the schedule starts today or in the past
        if (!effectiveFrom.isAfter(LocalDate.now())) {
            LocalDate checkEndDate = effectiveTo != null && effectiveTo.isBefore(LocalDate.now().plusDays(14))
                ? effectiveTo
                : LocalDate.now().plusDays(14);

            List<AppointmentSlot> existingSlots = appointmentSlotRepository
                .findByDoctor_IdDoctorAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                    doctorId, LocalDate.now(), checkEndDate);

            // Parse lunch times outside lambda
            final LocalTime lunchStart = request.getLunchStart();
            final LocalTime lunchEnd = request.getLunchEnd();

            boolean hasConflicts = existingSlots.stream()
                .anyMatch(slot -> {
                    // Check if slot is on the day being modified
                    int slotDayOfWeek = slot.getSlotDate().getDayOfWeek().getValue() % 7;
                    if (slotDayOfWeek != request.getDayOfWeek()) {
                        return false;
                    }

                    // Check if slot is booked or completed
                    if ("free".equals(slot.getStatus().getTitle()) ||
                        "cancelled".equals(slot.getStatus().getTitle())) {
                        return false;
                    }

                    // Check if slot time conflicts with new schedule
                    // Conflict exists if slot is OUTSIDE working hours or DURING lunch break

                    // Check if slot is before work start or after work end
                    if (slot.getStartTime().isBefore(request.getStartTime()) ||
                        slot.getEndTime().isAfter(request.getEndTime())) {
                        return true;
                    }

                    // Check if slot overlaps with lunch break
                    if (lunchStart != null && lunchEnd != null) {
                        if (slot.getStartTime().isBefore(lunchEnd) &&
                            slot.getEndTime().isAfter(lunchStart)) {
                            return true;
                        }
                    }

                    return false;
                });

            if (hasConflicts) {
                throw new BadRequestException(
                    "Изменение расписания конфликтует с существующими записями пациентов. " +
                    "Пожалуйста, отмените или перенесите записи перед изменением расписания.");
            }
        }
    }

    /**
     * Generate slots for a schedule
     */
    private void generateSlotsForSchedule(String userEmail, Schedule schedule) {
        LocalDate slotStartDate = schedule.getEffectiveFrom() != null &&
                                  schedule.getEffectiveFrom().isAfter(LocalDate.now())
            ? schedule.getEffectiveFrom()
            : LocalDate.now();

        LocalDate slotEndDate = schedule.getEffectiveTo() != null &&
                                schedule.getEffectiveTo().isBefore(slotStartDate.plusDays(30))
            ? schedule.getEffectiveTo()
            : slotStartDate.plusDays(30);

        GenerateSlotsRequest generateRequest = new GenerateSlotsRequest();
        generateRequest.setStartDate(slotStartDate);
        generateRequest.setEndDate(slotEndDate);
        generateRequest.setSlotDurationMinutes(schedule.getAppointmentDuration());

        try {
            slotManagementService.generateSlots(userEmail, generateRequest);
        } catch (Exception e) {
            // Log error but don't interrupt schedule creation/update
        }
    }
}
