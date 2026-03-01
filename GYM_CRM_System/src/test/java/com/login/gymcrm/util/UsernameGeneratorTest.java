package com.login.gymcrm.util;

import com.login.gymcrm.config.AppConfig;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameGeneratorTest {

    @Test
    void generatesUniqueUsernameAcrossAllUsers() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            UsernameGenerator generator = context.getBean(UsernameGenerator.class);
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);

            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String firstName = "John" + suffix;
            String lastName = "Smith" + suffix;
            String expectedBase = firstName + "." + lastName;

            traineeService.createProfile(firstName, lastName);
            trainerService.createProfile(firstName, lastName, "Cardio");

            String username = generator.generate(firstName, lastName);
            assertThat(username).isEqualTo(expectedBase + "2");
        }
    }
}
