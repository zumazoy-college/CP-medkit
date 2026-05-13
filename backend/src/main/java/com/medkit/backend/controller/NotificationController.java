package com.medkit.backend.controller;

import com.medkit.backend.dto.response.NotificationResponse;
import com.medkit.backend.service.NotificationService;
import com.medkit.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userEmail);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(Pageable pageable) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        Page<NotificationResponse> notifications = notificationService.getAllNotifications(userEmail, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        notificationService.markAsRead(notificationId, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Void> markAllAsRead() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        notificationService.markAllAsRead(userEmail);
        return ResponseEntity.noContent().build();
    }

    // Test endpoint to create a notification for current user
    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<NotificationResponse> createTestNotification() {
        NotificationResponse notification = notificationService.createTestNotification(
                SecurityUtils.getCurrentUserEmail()
        );
        return ResponseEntity.ok(notification);
    }
}
