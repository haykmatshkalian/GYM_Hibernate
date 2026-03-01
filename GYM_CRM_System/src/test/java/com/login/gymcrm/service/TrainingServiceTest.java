package com.login.gymcrm.service;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingServiceTest {

    @Test
    void createTrainingUsesTrainingTypeAndCascadeDeleteFromTrainee() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            Trainee trainee = traineeService.createProfile("Ana", "Mills");
            Trainer trainer = trainerService.createProfile("Leo", "King", "Strength");

            Training created = trainingService.createTraining(
                    trainee.getId(),
                    trainer.getId(),
                    "Intro Session",
                    LocalDate.now(),
                    45
            );

            Training selected = trainingService.selectTraining(created.getId());
            assertThat(selected.getTrainingType().getName()).isEqualTo("Strength");
            assertThat(selected.getDurationMinutes()).isEqualTo(45);

            traineeService.deleteProfile(trainee.getId());

            assertThat(trainingService.listAll())
                    .extracting(Training::getId)
                    .doesNotContain(created.getId());
            assertThatThrownBy(() -> trainingService.selectTraining(created.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
