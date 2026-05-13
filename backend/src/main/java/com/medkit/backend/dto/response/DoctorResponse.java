package com.medkit.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Integer idDoctor;
    private Integer userId;
    private String email;
    private String lastName;
    private String firstName;
    private String middleName;
    private String phoneNumber;
    private String avatarUrl;
    private String specialization;
    private BigDecimal rating;
    private Integer reviewsCount;
    private String office;
    private String workExperience;
    private Integer experienceYears;
    private String gender;
    private String nextAvailableSlot;
    private Boolean notifyCancellations;
    private Boolean notifyBookings;
}
