package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment_diagnoses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"appointment_id", "diagnosis_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_appointment_diagnosis")
    private Integer idAppointmentDiagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
}
