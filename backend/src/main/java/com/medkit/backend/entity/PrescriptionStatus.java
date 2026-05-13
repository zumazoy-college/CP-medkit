package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prescription_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    private Integer idStatus;

    @Column(nullable = false, unique = true, length = 20)
    private String title;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
