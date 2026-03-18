package com.login.gymcrm.dto;

import java.time.LocalDate;

public record TrainerTrainingItemResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        int trainingDuration,
        String traineeName
) {
}
