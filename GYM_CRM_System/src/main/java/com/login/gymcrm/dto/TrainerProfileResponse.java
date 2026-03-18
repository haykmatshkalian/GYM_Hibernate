package com.login.gymcrm.dto;

import java.util.List;

public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        String specialization,
        boolean isActive,
        List<TraineeSummaryResponse> trainees
) {
}
