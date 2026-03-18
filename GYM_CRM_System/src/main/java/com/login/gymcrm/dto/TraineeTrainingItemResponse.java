package com.login.gymcrm.dto;

import java.time.LocalDate;

public record TraineeTrainingItemResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        int trainingDuration,
        String trainerName
) {
}
