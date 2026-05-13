package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_patient")
    private Integer idPatient;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column(nullable = false, unique = true, length = 11)
    private String snils;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseases;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentSlot> appointmentSlots;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "favorites",
        joinColumns = @JoinColumn(name = "patient_id"),
        inverseJoinColumns = @JoinColumn(name = "doctor_id")
    )
    private List<Doctor> favoriteDoctors;
}
