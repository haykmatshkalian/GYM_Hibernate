package com.login.gymcrm.monitoring.health;

import com.login.gymcrm.service.TrainingService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomServiceHealthIndicator implements HealthIndicator {

    private final TrainingService trainingService;

    public CustomServiceHealthIndicator(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Override
    public Health health() {
        try {
            int trainingTypes = trainingService.listTrainingTypes().size();
            return Health.up()
                    .withDetail("service", "training-service")
                    .withDetail("trainingTypesCount", trainingTypes)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("service", "training-service")
                    .build();
        }
    }
}
