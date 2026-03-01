package com.login.gymcrm.util;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameGeneratorTest {

    @Test
    void generatesUniqueUsernameAcrossAllUsers() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            UsernameGenerator generator = context.getBean(UsernameGenerator.class);
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);

            traineeService.createProfile("John", "Smith");
            trainerService.createProfile("John", "Smith", "Cardio");

            String username = generator.generate("John", "Smith");
            assertThat(username).isEqualTo("John.Smith2");
        }
    }
}
