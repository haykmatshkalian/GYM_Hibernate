package com.login.gymcrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record TrainerTrainingItemResponse(
        String trainingName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate trainingDate,
        String trainingType,
        int trainingDuration,
        String traineeName
) {
}
