package com.medkit.backend.controller;

import com.medkit.backend.dto.response.AnalysisResponse;
import com.medkit.backend.entity.Analysis;
import com.medkit.backend.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisRepository analysisRepository;

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AnalysisResponse>> searchAnalyses(
            @RequestParam String query,
            Pageable pageable) {
        Page<AnalysisResponse> analyses = analysisRepository
                .findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCase(query, query, pageable)
                .map(this::mapToResponse);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AnalysisResponse> getAnalysisById(@PathVariable Integer id) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Анализ не найден"));
        return ResponseEntity.ok(mapToResponse(analysis));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AnalysisResponse> getAnalysisByCode(@PathVariable String code) {
        Analysis analysis = analysisRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Анализ не найден"));
        return ResponseEntity.ok(mapToResponse(analysis));
    }

    private AnalysisResponse mapToResponse(Analysis analysis) {
        AnalysisResponse response = new AnalysisResponse();
        response.setIdAnalysis(analysis.getIdAnalysis());
        response.setCode(analysis.getCode());
        response.setTitle(analysis.getTitle());
        response.setDescription(analysis.getDescription());
        return response;
    }
}
