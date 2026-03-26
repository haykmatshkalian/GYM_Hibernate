package com.login.gymcrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record TraineeProfileResponse(
        String username,
        String firstName,
        String lastName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerSummaryResponse> trainers
) {
}
