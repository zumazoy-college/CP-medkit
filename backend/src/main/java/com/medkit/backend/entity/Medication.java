package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medication")
    private Integer idMedication;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "active_substance", nullable = false, length = 255)
    private String activeSubstance;

    @Column(length = 255)
    private String manufacturer;

    @Column(length = 100)
    private String form;
}
