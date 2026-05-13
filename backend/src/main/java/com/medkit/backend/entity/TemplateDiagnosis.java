package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_diagnoses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "diagnosis_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_template_diagnosis")
    private Integer idTemplateDiagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
}
