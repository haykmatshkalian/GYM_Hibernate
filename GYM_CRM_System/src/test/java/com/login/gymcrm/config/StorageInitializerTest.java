package com.login.gymcrm.config;

import com.login.gymcrm.facade.GymCrmFacade;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StorageInitializerTest {

    @Test
    void contextLoadsAndPersistsEntitiesWithCorrectPrimitiveTypes() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            Trainee trainee = facade.createTrainee("John", "Smith");
            Trainer trainer = facade.createTrainer("Sarah", "Connor", "Strength");
            Training training = facade.createTraining(trainee.getId(), trainer.getId(), "Session", LocalDate.now(), 60);

            assertThat(trainee.isActive()).isTrue();
            assertThat(training.getDate()).isInstanceOf(LocalDate.class);
            assertThat(training.getDurationMinutes()).isEqualTo(60);
        }
    }
}
