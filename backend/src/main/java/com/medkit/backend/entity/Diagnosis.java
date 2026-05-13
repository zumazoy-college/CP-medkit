package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diagnoses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diagnosis")
    private Integer idDiagnosis;

    @Column(name = "icd_code", nullable = false, unique = true, length = 10)
    private String icdCode;

    @Column(name = "icd_name", nullable = false, length = 500)
    private String icdName;
}
