package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_settings")
    private Integer idSettings;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "notify_appointment_reminder")
    private Boolean notifyAppointmentReminder = true;

    @Column(name = "notify_rating_reminder")
    private Boolean notifyRatingReminder = true;

    @Column(name = "notify_appointment_cancelled")
    private Boolean notifyAppointmentCancelled = true;

    @Column(name = "default_screen", length = 50)
    private String defaultScreen = "search";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
