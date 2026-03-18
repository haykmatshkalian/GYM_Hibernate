package com.login.gymcrm.dto;

public record TrainerSummaryResponse(
        String username,
        String firstName,
        String lastName,
        String specialization
) {
}
