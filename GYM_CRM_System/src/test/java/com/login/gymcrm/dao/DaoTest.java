package com.login.gymcrm.dao;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaoTest {

    @Test
    void updateTraineeTrainerListPersistsManyToManyRows() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TraineeDao traineeDao = context.getBean(TraineeDao.class);

            Trainee trainee = traineeService.createProfile("Alex", "Stone");
            Trainer trainerOne = trainerService.createProfile("John", "Ray", "Cardio");
            Trainer trainerTwo = trainerService.createProfile("Sarah", "Kay", "Yoga");

            traineeService.updateTrainersList(trainee.getId(), List.of(trainerOne.getId(), trainerTwo.getId()));

            Trainee reloaded = traineeDao.findById(trainee.getId()).orElseThrow();
            assertThat(reloaded.getTrainers()).extracting(Trainer::getId)
                    .containsExactlyInAnyOrder(trainerOne.getId(), trainerTwo.getId());
        }
    }
}
