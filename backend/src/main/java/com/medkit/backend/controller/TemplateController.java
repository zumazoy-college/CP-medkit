package com.medkit.backend.controller;

import com.medkit.backend.dto.request.CreateTemplateRequest;
import com.medkit.backend.dto.request.UpdateTemplateRequest;
import com.medkit.backend.dto.response.TemplateResponse;
import com.medkit.backend.service.TemplateService;
import com.medkit.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        TemplateResponse response = templateService.createTemplate(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<TemplateResponse>> getMyTemplates() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<TemplateResponse> templates = templateService.getDoctorTemplates(userEmail);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable Integer id) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        TemplateResponse template = templateService.getTemplateById(userEmail, id);
        return ResponseEntity.ok(template);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTemplateRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        TemplateResponse response = templateService.updateTemplate(userEmail, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Integer id) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        templateService.deleteTemplate(userEmail, id);
        return ResponseEntity.noContent().build();
    }
}
