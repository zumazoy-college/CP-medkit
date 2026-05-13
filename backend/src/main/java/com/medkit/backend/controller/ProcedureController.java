package com.medkit.backend.controller;

import com.medkit.backend.dto.response.ProcedureResponse;
import com.medkit.backend.entity.Procedure;
import com.medkit.backend.repository.ProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procedures")
@RequiredArgsConstructor
public class ProcedureController {

    private final ProcedureRepository procedureRepository;

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<ProcedureResponse>> searchProcedures(
            @RequestParam String query,
            Pageable pageable) {
        Page<ProcedureResponse> procedures = procedureRepository
                .findByTitleContainingIgnoreCase(query, pageable)
                .map(this::mapToResponse);
        return ResponseEntity.ok(procedures);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ProcedureResponse> getProcedureById(@PathVariable Integer id) {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Процедура не найдена"));
        return ResponseEntity.ok(mapToResponse(procedure));
    }

    private ProcedureResponse mapToResponse(Procedure procedure) {
        ProcedureResponse response = new ProcedureResponse();
        response.setIdProcedure(procedure.getIdProcedure());
        response.setTitle(procedure.getTitle());
        response.setDescription(procedure.getDescription());
        response.setDuration(procedure.getDuration());
        return response;
    }
}
