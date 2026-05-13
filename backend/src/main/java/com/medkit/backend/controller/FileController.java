package com.medkit.backend.controller;

import com.medkit.backend.dto.response.FileResponse;
import com.medkit.backend.service.FileService;
import com.medkit.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam Integer appointmentId,
            @RequestParam("file") MultipartFile file) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        FileResponse response = fileService.uploadFile(userEmail, appointmentId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<FileResponse>> getAppointmentFiles(@PathVariable Integer appointmentId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<FileResponse> files = fileService.getAppointmentFiles(userEmail, appointmentId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<FileResponse>> getMyFiles() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<FileResponse> files = fileService.getPatientFiles(userEmail);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/download/{fileId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer fileId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        Resource resource = fileService.downloadFile(userEmail, fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteFile(@PathVariable Integer fileId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        fileService.deleteFile(userEmail, fileId);
        return ResponseEntity.noContent().build();
    }
}
