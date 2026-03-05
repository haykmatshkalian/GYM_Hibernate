package com.login.gymcrm.service;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

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

    @Test
    void returnsTraineeTrainingsByUsernameAndCriteria() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Trainee trainee = traineeService.createProfile("Trainee" + suffix, "One" + suffix);
            Trainer cardioTrainer = trainerService.createProfile("Cardio" + suffix, "Coach" + suffix, "Cardio");
            Trainer yogaTrainer = trainerService.createProfile("Yoga" + suffix, "Coach" + suffix, "Yoga");

            Training first = trainingService.createTraining(
                    trainee.getId(),
                    cardioTrainer.getId(),
                    "Cardio Day",
                    LocalDate.of(2026, 3, 10),
                    40
            );
            Training second = trainingService.createTraining(
                    trainee.getId(),
                    yogaTrainer.getId(),
                    "Yoga Day",
                    LocalDate.of(2026, 3, 20),
                    50
            );

            assertThat(first.getId()).isNotEqualTo(second.getId());

            assertThat(trainingService.getTraineeTrainingsByCriteria(
                    trainee.getUsername(),
                    LocalDate.of(2026, 3, 15),
                    LocalDate.of(2026, 3, 30),
                    yogaTrainer.getFirstName(),
                    "Yoga"
            )).extracting(Training::getId)
                    .containsExactly(second.getId());
        }
    }

    @Test
    void returnsTrainerTrainingsByUsernameAndCriteria() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Trainer trainer = trainerService.createProfile("Main" + suffix, "Coach" + suffix, "CrossFit");
            Trainee firstTrainee = traineeService.createProfile("Target" + suffix, "Client" + suffix);
            Trainee secondTrainee = traineeService.createProfile("Other" + suffix, "Client" + suffix);

            Training inRange = trainingService.createTraining(
                    firstTrainee.getId(),
                    trainer.getId(),
                    "Target Session",
                    LocalDate.of(2026, 2, 10),
                    35
            );
            trainingService.createTraining(
                    secondTrainee.getId(),
                    trainer.getId(),
                    "Other Session",
                    LocalDate.of(2026, 2, 12),
                    35
            );
            trainingService.createTraining(
                    firstTrainee.getId(),
                    trainer.getId(),
                    "Old Session",
                    LocalDate.of(2026, 1, 20),
                    35
            );

            assertThat(trainingService.getTrainerTrainingsByCriteria(
                    trainer.getUsername(),
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 2, 28),
                    firstTrainee.getFirstName()
            )).extracting(Training::getId)
                    .containsExactly(inRange.getId());
        }
    }

    @Test
    void rejectsInvalidCriteriaDateRange() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Trainee trainee = traineeService.createProfile("Range" + suffix, "Check" + suffix);

            assertThatThrownBy(() -> trainingService.getTraineeTrainingsByCriteria(
                    trainee.getUsername(),
                    LocalDate.of(2026, 4, 2),
                    LocalDate.of(2026, 4, 1),
                    null,
                    null
            )).isInstanceOf(ValidationException.class);
        }
    }
}
