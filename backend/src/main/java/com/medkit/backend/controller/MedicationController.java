package com.medkit.backend.controller;

import com.medkit.backend.dto.response.MedicationResponse;
import com.medkit.backend.entity.Medication;
import com.medkit.backend.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationRepository medicationRepository;

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<MedicationResponse>> searchMedications(
            @RequestParam String query,
            Pageable pageable) {
        Page<MedicationResponse> medications = medicationRepository
                .findByTitleContainingIgnoreCaseOrActiveSubstanceContainingIgnoreCase(query, query, pageable)
                .map(this::mapToResponse);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicationResponse> getMedicationById(@PathVariable Integer id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Лекарство не найдено"));
        return ResponseEntity.ok(mapToResponse(medication));
    }

    private MedicationResponse mapToResponse(Medication medication) {
        MedicationResponse response = new MedicationResponse();
        response.setIdMedication(medication.getIdMedication());
        response.setTitle(medication.getTitle());
        response.setActiveSubstance(medication.getActiveSubstance());
        response.setManufacturer(medication.getManufacturer());
        response.setForm(medication.getForm());
        return response;
    }
}
