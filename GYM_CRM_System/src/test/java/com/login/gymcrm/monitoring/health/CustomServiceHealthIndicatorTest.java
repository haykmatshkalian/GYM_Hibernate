package com.login.gymcrm.monitoring.health;

import com.login.gymcrm.model.TrainingType;
import com.login.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomServiceHealthIndicatorTest {

    @Mock
    private TrainingService trainingService;

    @Test
    void healthIsUpWhenTrainingServiceResponds() {
        when(trainingService.listTrainingTypes()).thenReturn(List.of(new TrainingType(UUID.randomUUID().toString(), "Cardio")));
        CustomServiceHealthIndicator indicator = new CustomServiceHealthIndicator(trainingService);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails()).containsEntry("service", "training-service");
        assertThat(indicator.health().getDetails()).containsEntry("trainingTypesCount", 1);
    }

    @Test
    void healthIsDownWhenTrainingServiceFails() {
        when(trainingService.listTrainingTypes()).thenThrow(new RuntimeException("service down"));
        CustomServiceHealthIndicator indicator = new CustomServiceHealthIndicator(trainingService);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("service", "training-service");
    }
}
