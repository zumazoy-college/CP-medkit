package com.medkit.backend.dto.request;

import lombok.Data;

@Data
public class UpdateNotificationSettingsRequest {
    private Boolean notifyCancellations;
    private Boolean notifyBookings;
}
