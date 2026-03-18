package com.login.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTraineeProfileRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        LocalDate dateOfBirth,
        String address,
        @NotNull(message = "Is active is required")
        Boolean isActive
) {
}
