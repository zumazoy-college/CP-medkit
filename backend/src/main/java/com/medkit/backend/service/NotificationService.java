package com.medkit.backend.service;

import com.medkit.backend.dto.response.NotificationResponse;
import com.medkit.backend.entity.Notification;
import com.medkit.backend.entity.User;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.exception.UnauthorizedException;
import com.medkit.backend.repository.NotificationRepository;
import com.medkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(String userEmail) {
        log.info("Getting unread notifications for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        List<Notification> notifications = notificationRepository.findByUser_IdUserAndIsReadFalse(user.getIdUser());
        log.info("Found {} unread notifications for userId={}", notifications.size(), user.getIdUser());

        return notifications
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(String userEmail, Pageable pageable) {
        log.info("Getting all notifications for user: {}, page: {}", userEmail, pageable.getPageNumber());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Page<NotificationResponse> notifications = notificationRepository.findByUser_IdUserOrderByCreatedAtDesc(user.getIdUser(), pageable)
                .map(this::mapToResponse);
        log.info("Found {} notifications for userId={}", notifications.getTotalElements(), user.getIdUser());

        return notifications;
    }

    @Transactional
    public void markAsRead(Integer notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление не найдено"));

        if (!notification.getUser().getIdUser().equals(user.getIdUser())) {
            throw new UnauthorizedException("Вы не можете изменить это уведомление");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        List<Notification> unreadNotifications = notificationRepository.findByUser_IdUserAndIsReadFalse(user.getIdUser());
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public NotificationResponse createNotification(Integer userId, String type, String title, String message) {
        log.info("Creating notification for userId={}, type={}, title={}", userId, type, title);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(message);
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification saved with id={} for userId={}", savedNotification.getIdNotification(), userId);

        return mapToResponse(savedNotification);
    }

    @Transactional
    public NotificationResponse createTestNotification(String userEmail) {
        log.info("Creating test notification for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        return createNotification(
                user.getIdUser(),
                "test",
                "Тестовое уведомление",
                "Это тестовое уведомление для проверки работы системы уведомлений"
        );
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setIdNotification(notification.getIdNotification());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getBody());
        response.setIsRead(notification.getIsRead());
        response.setLink(notification.getLink());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
