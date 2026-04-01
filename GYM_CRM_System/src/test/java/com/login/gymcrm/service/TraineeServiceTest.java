package com.login.gymcrm.service;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraineeServiceTest {

    @Test
    void createUpdateChangeStateAndDeleteTrainee() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService service = context.getBean(TraineeService.class);

            Trainee created = service.createProfile("Alice", "Brown");
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getUsername()).startsWith("Alice.Brown");
            assertThat(created.getUser().getGeneratedPassword()).hasSize(10);
            assertThat(created.getPassword()).startsWith("$2");
            assertThat(created.isActive()).isTrue();

            created.setFirstName("Alicia");
            Trainee updated = service.updateProfile(created);
            assertThat(updated.getFirstName()).isEqualTo("Alicia");

            Trainee toggled = service.changeState(created.getId());
            assertThat(toggled.isActive()).isFalse();

            service.deleteProfile(created.getId());
            assertThat(service.listAll()).doesNotContain(toggled);
        }
    }

    @Test
    void returnsTrainersNotAssignedToTraineeByUsername() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);

            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Trainee trainee = traineeService.createProfile("Alex" + suffix, "Stone" + suffix);
            Trainer assignedTrainer = trainerService.createProfile("John" + suffix, "Ray" + suffix, "Cardio");
            Trainer unassignedTrainer = trainerService.createProfile("Sarah" + suffix, "Kay" + suffix, "Yoga");

            traineeService.updateTrainersList(trainee.getId(), List.of(assignedTrainer.getId()));

            assertThat(traineeService.listUnassignedTrainersByTraineeUsername(trainee.getUsername()))
                    .extracting(Trainer::getId)
                    .contains(unassignedTrainer.getId())
                    .doesNotContain(assignedTrainer.getId());
        }
    }

    @Test
    void unassignedTrainersLookupFailsForUnknownUsername() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService service = context.getBean(TraineeService.class);

            assertThatThrownBy(() -> service.listUnassignedTrainersByTraineeUsername("missing.user"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Test
    void validationFailsOnMissingName() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService service = context.getBean(TraineeService.class);
            assertThatThrownBy(() -> service.createProfile("", ""))
                    .isInstanceOf(ValidationException.class);
        }
    }
}
