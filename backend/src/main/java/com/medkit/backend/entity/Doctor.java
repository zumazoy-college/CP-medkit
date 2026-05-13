package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doctor")
    private Integer idDoctor;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;

    @Column(length = 10)
    private String office;

    @Column(name = "work_experience")
    private LocalDate workExperience;

    @Column(name = "next_available_slot_date")
    private LocalDate nextAvailableSlotDate;

    @Column(name = "next_available_slot_time")
    private java.time.LocalTime nextAvailableSlotTime;

    @Column(name = "notify_cancellations")
    private Boolean notifyCancellations = true;

    @Column(name = "notify_bookings")
    private Boolean notifyBookings = true;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleException> exceptions;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentSlot> appointmentSlots;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Template> templates;

    @ManyToMany(mappedBy = "favoriteDoctors", fetch = FetchType.LAZY)
    private List<Patient> favoriteByPatients;
}
