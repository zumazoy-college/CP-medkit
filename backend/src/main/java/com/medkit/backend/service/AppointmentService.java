package com.medkit.backend.service;

import com.medkit.backend.dto.request.UpdateAppointmentRequest;
import com.medkit.backend.dto.response.*;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.exception.UnauthorizedException;
import com.medkit.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SlotStatusRepository slotStatusRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> getAvailableSlots(Integer doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        return slotRepository.findAvailableSlots(doctorId, date)
                .stream()
                .filter(slot -> {
                    // Filter out past slots
                    if (slot.getSlotDate().isBefore(today)) {
                        return false;
                    }
                    // For today, only show future time slots
                    if (slot.getSlotDate().isEqual(today)) {
                        return slot.getStartTime().isAfter(currentTime);
                    }
                    return true;
                })
                .map(this::mapSlotToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentSlotResponse bookAppointment(String userEmail, Integer doctorId,
                                                    LocalDate slotDate, LocalTime startTime) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        // Validate that the slot is not in the past
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (slotDate.isBefore(today)) {
            throw new BadRequestException("Нельзя записаться на прошедшую дату");
        }

        if (slotDate.isEqual(today) && startTime.isBefore(currentTime)) {
            throw new BadRequestException("Нельзя записаться на прошедшее время");
        }

        AppointmentSlot slot = slotRepository
                .findByDoctor_IdDoctorAndSlotDateAndStartTime(doctorId, slotDate, startTime)
                .orElseThrow(() -> new ResourceNotFoundException("Слот не найден"));

        if (!"free".equals(slot.getStatus().getTitle())) {
            throw new BadRequestException("Слот уже занят");
        }

        // Delete any existing appointment for this slot using direct query
        appointmentRepository.deleteBySlotId(slot.getIdSlot());

        SlotStatus bookedStatus = slotStatusRepository.findByTitle("booked")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'booked' не найден"));

        slot.setStatus(bookedStatus);
        slot.setPatient(patient);

        // Clear cancellation info if slot was previously cancelled
        slot.setCancellationReason(null);
        slot.setCancelledAt(null);

        AppointmentSlot savedSlot = slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setSlot(savedSlot);
        appointment.setPatient(patient);
        appointmentRepository.save(appointment);

        // Create notification for doctor only if enabled
        if (doctor.getNotifyBookings() != null && doctor.getNotifyBookings()) {
            String patientName = patient.getUser().getLastName() + " " + patient.getUser().getFirstName();
            String notificationTitle = "Новая запись на прием";
            String notificationMessage = String.format("Пациент %s записался на прием %s в %s",
                    patientName,
                    slotDate.toString(),
                    startTime.toString());

            notificationService.createNotification(
                    doctor.getUser().getIdUser(),
                    "appointment_booked",
                    notificationTitle,
                    notificationMessage
            );
        }

        return mapSlotToResponse(savedSlot);
    }

    @Transactional
    public void cancelAppointment(String userEmail, Integer slotId, String reason) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Слот не найден"));

        if (slot.getPatient() != null && !slot.getPatient().getUser().getIdUser().equals(user.getIdUser())) {
            if (!user.getRole().equals(User.UserRole.doctor) ||
                !slot.getDoctor().getUser().getIdUser().equals(user.getIdUser())) {
                throw new UnauthorizedException("Вы не можете отменить этот прием");
            }
        }

        SlotStatus freeStatus = slotStatusRepository.findByTitle("free")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'free' не найден"));

        // Save cancellation info before freeing the slot
        slot.setCancellationReason(reason);
        slot.setCancelledAt(LocalDateTime.now());

        // Store patient reference before clearing
        Patient affectedPatient = slot.getPatient();

        // Free the slot for other patients
        slot.setStatus(freeStatus);
        slot.setPatient(null);
        slotRepository.save(slot);

        // Send notification to doctor if patient cancelled and notifications are enabled
        if (user.getRole().equals(User.UserRole.patient) && slot.getDoctor() != null) {
            if (slot.getDoctor().getNotifyCancellations() != null && slot.getDoctor().getNotifyCancellations()) {
                String patientName = user.getLastName() + " " + user.getFirstName();
                String notificationTitle = "Отмена записи";
                String notificationMessage = String.format("Пациент %s отменил запись на %s в %s. Причина: %s",
                        patientName,
                        slot.getSlotDate().toString(),
                        slot.getStartTime().toString(),
                        reason);

                notificationService.createNotification(
                        slot.getDoctor().getUser().getIdUser(),
                        "appointment_cancelled",
                        notificationTitle,
                        notificationMessage
                );
            }
        }

        // Send notification to patient if doctor cancelled
        if (user.getRole().equals(User.UserRole.doctor) && affectedPatient != null) {
            // Check if patient has notifications enabled
            UserSettings patientSettings = userSettingsRepository.findByUser_IdUser(affectedPatient.getUser().getIdUser())
                    .orElse(null);

            log.info("Patient {} cancellation notification settings: {}",
                    affectedPatient.getIdPatient(),
                    patientSettings != null ? patientSettings.getNotifyAppointmentCancelled() : "null");

            // Only send notification if patient has this notification type enabled (default true if no settings)
            boolean shouldNotify = patientSettings == null ||
                                   patientSettings.getNotifyAppointmentCancelled() == null ||
                                   patientSettings.getNotifyAppointmentCancelled();

            log.info("Should notify patient {} about cancellation: {}", affectedPatient.getIdPatient(), shouldNotify);

            if (shouldNotify) {
                String doctorName = user.getLastName() + " " + user.getFirstName();
                String notificationTitle = "Запись отменена";
                String notificationMessage = String.format("Врач %s отменил вашу запись на %s в %s. Причина: %s",
                        doctorName,
                        slot.getSlotDate().toString(),
                        slot.getStartTime().toString(),
                        reason);

                // Find the appointment to get its ID
                Appointment appointment = appointmentRepository.findBySlot_IdSlot(slotId).orElse(null);

                Notification notification = new Notification();
                notification.setUser(affectedPatient.getUser());
                notification.setType("appointment_cancelled");
                notification.setTitle(notificationTitle);
                notification.setBody(notificationMessage);
                notification.setIsRead(false);
                if (appointment != null) {
                    notification.setLink(String.valueOf(appointment.getIdAppointment()));
                }
                notificationRepository.save(notification);
                log.info("Sent cancellation notification to patient {}", affectedPatient.getIdPatient());
            } else {
                log.info("Skipped cancellation notification - patient has notifications disabled");
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getPatientAppointments(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        return appointmentRepository.findByPatient_IdPatientOrderByCreatedAtDesc(patient.getIdPatient(), pageable)
                .map(this::mapAppointmentToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return appointmentRepository.findByDoctorId(doctor.getIdDoctor(), pageable)
                .map(this::mapAppointmentToResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointmentsByDate(String userEmail, LocalDate date) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        return appointmentRepository.findByDoctorIdAndDate(doctor.getIdDoctor(), date)
                .stream()
                .map(this::mapAppointmentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse completeAppointment(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете завершить этот прием");
        }

        // Проверяем, что прием не был отменен
        if (appointment.getSlot().getCancellationReason() != null) {
            throw new BadRequestException("Невозможно завершить отмененный прием");
        }

        SlotStatus completedStatus = slotStatusRepository.findByTitle("completed")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'completed' не найден"));

        appointment.getSlot().setStatus(completedStatus);
        slotRepository.save(appointment.getSlot());

        // Check if patient has notifications enabled
        UserSettings patientSettings = userSettingsRepository.findByUser_IdUser(appointment.getPatient().getUser().getIdUser())
                .orElse(null);

        log.info("Patient {} rating reminder settings: {}",
                appointment.getPatient().getIdPatient(),
                patientSettings != null ? patientSettings.getNotifyRatingReminder() : "null");

        // Only send notification if patient has this notification type enabled (default true if no settings)
        boolean shouldNotify = patientSettings == null ||
                               patientSettings.getNotifyRatingReminder() == null ||
                               patientSettings.getNotifyRatingReminder();

        log.info("Should notify patient {} about rating: {}", appointment.getPatient().getIdPatient(), shouldNotify);

        if (shouldNotify) {
            // Send notification to patient asking to rate the doctor
            String doctorName = doctor.getUser().getLastName() + " " + doctor.getUser().getFirstName();
            String notificationTitle = "Как прошел прием?";
            String notificationMessage = String.format("Оцените врача %s", doctorName);

            // Create notification with appointmentId in link field for navigation
            Notification notification = new Notification();
            notification.setUser(appointment.getPatient().getUser());
            notification.setType("appointment_completed");
            notification.setTitle(notificationTitle);
            notification.setBody(notificationMessage);
            notification.setIsRead(false);
            notification.setLink(String.valueOf(appointment.getIdAppointment()));
            notificationRepository.save(notification);
            log.info("Sent rating reminder to patient {}", appointment.getPatient().getIdPatient());
        } else {
            log.info("Skipped rating reminder - patient has notifications disabled");
        }

        return mapAppointmentToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public DetailedAppointmentResponse getAppointmentById(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        // Проверяем, что пользователь - врач (любой) или пациент этого приема
        boolean isAnyDoctor = doctorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isAnyDoctor && !isPatient) {
            throw new UnauthorizedException("У вас нет доступа к этому приему");
        }

        return mapToDetailedResponse(appointment);
    }

    @Transactional
    public DetailedAppointmentResponse updateAppointment(String userEmail, Integer appointmentId,
                                                         UpdateAppointmentRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете редактировать этот прием");
        }

        // Проверяем, что прием не был отменен
        if (appointment.getSlot().getCancellationReason() != null) {
            throw new BadRequestException("Невозможно редактировать отмененный прием");
        }

        // Обновляем поля
        if (request.getComplaints() != null) {
            appointment.setComplaints(request.getComplaints());
        }
        if (request.getAnamnesis() != null) {
            appointment.setAnamnesis(request.getAnamnesis());
        }
        if (request.getObjectiveData() != null) {
            appointment.setObjectiveData(request.getObjectiveData());
        }
        if (request.getRecommendations() != null) {
            appointment.setRecommendations(request.getRecommendations());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDetailedResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<DetailedAppointmentResponse> getPatientHistory(String userEmail, Integer patientId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        // Получаем все завершенные приемы пациента, отсортированные по дате (свежие сверху)
        return appointmentRepository.findByPatient_IdPatientAndSlot_Status_TitleOrderByCreatedAtDesc(
                patientId, "completed")
                .stream()
                .map(this::mapToDetailedResponse)
                .collect(Collectors.toList());
    }

    private AppointmentSlotResponse mapSlotToResponse(AppointmentSlot slot) {
        AppointmentSlotResponse response = new AppointmentSlotResponse();
        response.setIdSlot(slot.getIdSlot());
        response.setDoctorId(slot.getDoctor().getIdDoctor());
        response.setDoctorName(slot.getDoctor().getUser().getLastName() + " " +
                               slot.getDoctor().getUser().getFirstName());
        response.setSlotDate(slot.getSlotDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setStatus(slot.getStatus().getTitle());
        response.setCreatedAt(slot.getCreatedAt());
        return response;
    }

    private AppointmentResponse mapAppointmentToResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setIdAppointment(appointment.getIdAppointment());
        response.setSlotId(appointment.getSlot().getIdSlot());
        response.setPatientId(appointment.getPatient().getIdPatient());
        response.setPatientName(appointment.getPatient().getUser().getLastName() + " " +
                               appointment.getPatient().getUser().getFirstName());

        // Заполняем объект patient
        AppointmentResponse.PatientInfo patientInfo = new AppointmentResponse.PatientInfo();
        patientInfo.setId(appointment.getPatient().getIdPatient());
        patientInfo.setFirstName(appointment.getPatient().getUser().getFirstName());
        patientInfo.setLastName(appointment.getPatient().getUser().getLastName());
        patientInfo.setMiddleName(appointment.getPatient().getUser().getMiddleName());
        patientInfo.setDateOfBirth(appointment.getPatient().getBirthdate());
        patientInfo.setSnils(appointment.getPatient().getSnils());
        response.setPatient(patientInfo);

        response.setDoctorId(appointment.getSlot().getDoctor().getIdDoctor());
        response.setDoctorName(appointment.getSlot().getDoctor().getUser().getLastName() + " " +
                              appointment.getSlot().getDoctor().getUser().getFirstName());
        response.setDoctorSpecialization(appointment.getSlot().getDoctor().getSpecialization());
        response.setDoctorOffice(appointment.getSlot().getDoctor().getOffice());
        response.setSlotDate(appointment.getSlot().getSlotDate());
        response.setStartTime(appointment.getSlot().getStartTime());
        response.setEndTime(appointment.getSlot().getEndTime());

        // Determine status: if slot has cancellation reason, it was cancelled
        String status = appointment.getSlot().getCancellationReason() != null
                ? "cancelled"
                : appointment.getSlot().getStatus().getTitle();
        response.setStatus(status);

        response.setHasReview(reviewRepository.existsByAppointment_IdAppointment(appointment.getIdAppointment()));

        // Get review ID, rating, comment, and edit/delete permissions if exists
        reviewRepository.findByAppointment_IdAppointment(appointment.getIdAppointment())
                .ifPresent(review -> {
                    response.setReviewId(review.getIdReview());
                    response.setReviewRating(review.getRating());
                    response.setReviewComment(review.getComment());

                    // Check if review can be edited/deleted (within 24 hours)
                    long hoursSinceCreation = Duration.between(review.getCreatedAt(), LocalDateTime.now()).toHours();
                    boolean canModify = hoursSinceCreation < 24;
                    response.setCanEditReview(canModify);
                    response.setCanDeleteReview(canModify);
                });

        // Get primary diagnosis name if exists
        if (appointment.getDiagnoses() != null && !appointment.getDiagnoses().isEmpty()) {
            appointment.getDiagnoses().stream()
                    .filter(ad -> Boolean.TRUE.equals(ad.getIsPrimary()))
                    .findFirst()
                    .ifPresent(ad -> response.setPrimaryDiagnosisName(ad.getDiagnosis().getIcdName()));
        }

        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }

    private DetailedAppointmentResponse mapToDetailedResponse(Appointment appointment) {
        DetailedAppointmentResponse response = new DetailedAppointmentResponse();
        response.setIdAppointment(appointment.getIdAppointment());
        response.setSlotId(appointment.getSlot().getIdSlot());

        // Patient info
        DetailedAppointmentResponse.PatientInfo patientInfo = new DetailedAppointmentResponse.PatientInfo();
        patientInfo.setId(appointment.getPatient().getIdPatient());
        patientInfo.setFirstName(appointment.getPatient().getUser().getFirstName());
        patientInfo.setLastName(appointment.getPatient().getUser().getLastName());
        patientInfo.setMiddleName(appointment.getPatient().getUser().getMiddleName());
        patientInfo.setDateOfBirth(appointment.getPatient().getBirthdate());
        patientInfo.setSnils(appointment.getPatient().getSnils());
        response.setPatient(patientInfo);

        // Doctor info
        DetailedAppointmentResponse.DoctorInfo doctorInfo = new DetailedAppointmentResponse.DoctorInfo();
        doctorInfo.setId(appointment.getSlot().getDoctor().getIdDoctor());
        doctorInfo.setFirstName(appointment.getSlot().getDoctor().getUser().getFirstName());
        doctorInfo.setLastName(appointment.getSlot().getDoctor().getUser().getLastName());
        doctorInfo.setMiddleName(appointment.getSlot().getDoctor().getUser().getMiddleName());
        doctorInfo.setSpecialization(appointment.getSlot().getDoctor().getSpecialization());
        response.setDoctor(doctorInfo);

        response.setSlotDate(appointment.getSlot().getSlotDate());
        response.setStartTime(appointment.getSlot().getStartTime());
        response.setEndTime(appointment.getSlot().getEndTime());

        // Determine status: if slot has cancellation reason, it was cancelled
        String status = appointment.getSlot().getCancellationReason() != null
                ? "cancelled"
                : appointment.getSlot().getStatus().getTitle();
        response.setStatus(status);

        // Medical record fields
        response.setComplaints(appointment.getComplaints());
        response.setAnamnesis(appointment.getAnamnesis());
        response.setObjectiveData(appointment.getObjectiveData());
        response.setRecommendations(appointment.getRecommendations());

        // Diagnoses
        if (appointment.getDiagnoses() != null) {
            response.setDiagnoses(appointment.getDiagnoses().stream()
                    .map(this::mapDiagnosisToResponse)
                    .collect(Collectors.toList()));
        }

        // Medications
        if (appointment.getMedicationPrescriptions() != null) {
            response.setMedications(appointment.getMedicationPrescriptions().stream()
                    .map(this::mapMedicationToResponse)
                    .collect(Collectors.toList()));
        }

        // Procedures
        if (appointment.getProcedurePrescriptions() != null) {
            response.setProcedures(appointment.getProcedurePrescriptions().stream()
                    .map(this::mapProcedureToResponse)
                    .collect(Collectors.toList()));
        }

        // Analyses
        if (appointment.getAnalysisPrescriptions() != null) {
            response.setAnalyses(appointment.getAnalysisPrescriptions().stream()
                    .map(this::mapAnalysisToResponse)
                    .collect(Collectors.toList()));
        }

        // Files
        if (appointment.getFiles() != null) {
            response.setFiles(appointment.getFiles().stream()
                    .map(this::mapFileToResponse)
                    .collect(Collectors.toList()));
        }

        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }

    private AppointmentDiagnosisResponse mapDiagnosisToResponse(AppointmentDiagnosis ad) {
        AppointmentDiagnosisResponse response = new AppointmentDiagnosisResponse();
        response.setId(ad.getIdAppointmentDiagnosis());
        response.setAppointmentId(ad.getAppointment().getIdAppointment());
        response.setDiagnosisId(ad.getDiagnosis().getIdDiagnosis());
        response.setIcdCode(ad.getDiagnosis().getIcdCode());
        response.setDiagnosisName(ad.getDiagnosis().getIcdName());
        response.setIsPrimary(ad.getIsPrimary());
        return response;
    }

    private MedicationPrescriptionResponse mapMedicationToResponse(MedicationPrescription mp) {
        MedicationPrescriptionResponse response = new MedicationPrescriptionResponse();
        response.setId(mp.getIdMedicationPrescription());
        response.setAppointmentId(mp.getAppointment().getIdAppointment());
        response.setMedicationId(mp.getMedication().getIdMedication());
        response.setMedicationName(mp.getMedication().getTitle());
        response.setDosage(null); // Not in entity
        response.setFrequency(null); // Not in entity
        response.setDuration(mp.getDuration());
        response.setInstructions(mp.getInstructions());
        response.setStatus(mp.getStatus().getTitle());
        response.setCreatedAt(mp.getCreatedAt());
        return response;
    }

    private ProcedurePrescriptionResponse mapProcedureToResponse(ProcedurePrescription pp) {
        ProcedurePrescriptionResponse response = new ProcedurePrescriptionResponse();
        response.setId(pp.getIdProcedurePrescription());
        response.setAppointmentId(pp.getAppointment().getIdAppointment());
        response.setProcedureId(pp.getProcedure().getIdProcedure());
        response.setProcedureName(pp.getProcedure().getTitle());
        response.setInstructions(pp.getInstructions());
        response.setStatus(pp.getStatus().getTitle());
        response.setCreatedAt(pp.getCreatedAt());
        return response;
    }

    private AnalysisPrescriptionResponse mapAnalysisToResponse(AnalysisPrescription ap) {
        AnalysisPrescriptionResponse response = new AnalysisPrescriptionResponse();
        response.setId(ap.getIdAnalysisPrescription());
        response.setAppointmentId(ap.getAppointment().getIdAppointment());
        response.setAnalysisId(ap.getAnalysis().getIdAnalysis());
        response.setAnalysisName(ap.getAnalysis().getTitle());
        response.setInstructions(ap.getInstructions());
        response.setStatus(ap.getStatus().getTitle());
        response.setCreatedAt(ap.getCreatedAt());
        return response;
    }

    private FileResponse mapFileToResponse(FileEntity file) {
        FileResponse response = new FileResponse();
        response.setId(file.getIdFile());
        response.setAppointmentId(file.getAppointment().getIdAppointment());
        response.setFileName(file.getFileName());
        response.setFileType(null); // Not in entity
        response.setFileSize(null); // Not in entity
        response.setUploadedAt(file.getCreatedAt());
        return response;
    }
}
