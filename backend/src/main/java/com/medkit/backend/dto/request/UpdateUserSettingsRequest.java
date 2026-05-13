package com.medkit.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserSettingsRequest {

    @NotNull(message = "Укажите настройку напоминаний о приеме")
    private Boolean notifyAppointmentReminder;

    @NotNull(message = "Укажите настройку напоминаний об оценке")
    private Boolean notifyRatingReminder;

    @NotNull(message = "Укажите настройку уведомлений об отмене")
    private Boolean notifyAppointmentCancelled;

    @NotNull(message = "Укажите экран по умолчанию")
    private String defaultScreen;
}
