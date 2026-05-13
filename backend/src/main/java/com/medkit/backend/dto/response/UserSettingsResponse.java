package com.medkit.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {
    private Boolean notifyAppointmentReminder;
    private Boolean notifyRatingReminder;
    private Boolean notifyAppointmentCancelled;
    private String defaultScreen;
}
