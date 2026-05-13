package com.medkit.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsResponse {
    private Integer todayCompletedAppointments;
    private Integer weekCompletedAppointments;
}
