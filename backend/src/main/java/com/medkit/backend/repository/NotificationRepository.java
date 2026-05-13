package com.medkit.backend.repository;

import com.medkit.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByUser_IdUserOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    List<Notification> findByUser_IdUserAndIsReadFalse(Integer userId);

    long countByUser_IdUserAndIsReadFalse(Integer userId);
}
