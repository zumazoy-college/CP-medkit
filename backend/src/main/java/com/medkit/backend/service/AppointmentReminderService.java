package com.medkit.backend.service;

import com.medkit.backend.entity.Appointment;
import com.medkit.backend.entity.AppointmentSlot;
import com.medkit.backend.entity.Notification;
import com.medkit.backend.entity.UserSettings;
import com.medkit.backend.repository.AppointmentRepository;
import com.medkit.backend.repository.AppointmentSlotRepository;
import com.medkit.backend.repository.NotificationRepository;
import com.medkit.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderService {

    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final UserSettingsRepository userSettingsRepository;

    // Run every 10 minutes
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendAppointmentReminders() {
        log.info("Running appointment reminder check");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);

        // Get the date and time range for 2 hours from now (with 10-minute window)
        LocalDate targetDate = twoHoursLater.toLocalDate();
        LocalTime startTime = twoHoursLater.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(10);

        // Handle midnight crossing
        LocalDate nextDate = targetDate.plusDays(1);
        LocalTime nextDayStartTime = LocalTime.MIN; // 00:00
        LocalTime nextDayEndTime = LocalTime.MIN; // 00:00

        // If endTime crosses midnight (e.g., 23:50 + 15min = 00:05 next day)
        if (endTime.isBefore(startTime)) {
            nextDate = targetDate.plusDays(1);
            nextDayStartTime = LocalTime.MIN;
            nextDayEndTime = endTime;
            endTime = LocalTime.MAX; // Set current day end to 23:59:59
        }

        // Find all booked appointments in the next 2 hours (within 15-minute window)
        List<AppointmentSlot> upcomingSlots = slotRepository.findUpcomingAppointmentsForReminder(
                targetDate, startTime, endTime, nextDate, nextDayStartTime, nextDayEndTime);

        log.info("Found {} appointments to remind about", upcomingSlots.size());

        for (AppointmentSlot slot : upcomingSlots) {
            if (slot.getPatient() != null) {
                try {
                    // Check if patient has notifications enabled
                    UserSettings patientSettings = userSettingsRepository.findByUser_IdUser(slot.getPatient().getUser().getIdUser())
                            .orElse(null);

                    log.info("Patient {} settings: {}",
                            slot.getPatient().getIdPatient(),
                            patientSettings != null ? patientSettings.getNotifyAppointmentReminder() : "null");

                    // Only send notification if patient has this notification type enabled (default true if no settings)
                    boolean shouldNotify = patientSettings == null ||
                                           patientSettings.getNotifyAppointmentReminder() == null ||
                                           patientSettings.getNotifyAppointmentReminder();

                    log.info("Should notify patient {}: {}", slot.getPatient().getIdPatient(), shouldNotify);

                    if (shouldNotify) {
                        String doctorName = slot.getDoctor().getUser().getLastName() + " " +
                                slot.getDoctor().getUser().getFirstName();
                        String notificationTitle = "Напоминание о приеме";
                        String notificationMessage = String.format(
                                "Напоминаем, что у вас прием к врачу %s через 2 часа (%s в %s)",
                                doctorName,
                                slot.getSlotDate().toString(),
                                slot.getStartTime().toString()
                        );

                        // Find the appointment to get its ID
                        Appointment appointment = appointmentRepository.findBySlot_IdSlot(slot.getIdSlot()).orElse(null);

                        Notification notification = new Notification();
                        notification.setUser(slot.getPatient().getUser());
                        notification.setType("appointment_reminder");
                        notification.setTitle(notificationTitle);
                        notification.setBody(notificationMessage);
                        notification.setIsRead(false);
                        if (appointment != null) {
                            notification.setLink(String.valueOf(appointment.getIdAppointment()));
                        }
                        notificationRepository.save(notification);

                        log.info("Sent reminder for appointment slot {} to patient {}",
                                slot.getIdSlot(), slot.getPatient().getIdPatient());
                    } else {
                        log.info("Skipped reminder for appointment slot {} - patient has notifications disabled",
                                slot.getIdSlot());
                    }
                } catch (Exception e) {
                    log.error("Failed to send reminder for slot {}: {}", slot.getIdSlot(), e.getMessage());
                }
            }
        }
    }
}
