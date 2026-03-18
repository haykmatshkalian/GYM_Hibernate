package com.login.gymcrm.controller;

import com.login.gymcrm.dto.AddTrainingRequest;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.dto.TrainingTypeResponse;
import com.login.gymcrm.mapper.RestDtoMapper;
import com.login.gymcrm.model.TrainingType;
import com.login.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private RestDtoMapper mapper;

    @InjectMocks
    private TrainingController trainingController;

    @Test
    void addTrainingReturnsOk() {
        AddTrainingRequest request = new AddTrainingRequest(
                "john.smith",
                "coach.sarah",
                "Session",
                LocalDate.of(2026, 1, 1),
                60
        );

        ResponseEntity<MessageResponse> response = trainingController.addTraining(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new MessageResponse("Training added"));
        verify(trainingService).createTrainingByUsernames("john.smith", "coach.sarah", "Session", LocalDate.of(2026, 1, 1), 60);
    }

    @Test
    void getTrainingTypesReturnsMappedList() {
        TrainingType type = new TrainingType(UUID.randomUUID().toString(), "Cardio");
        TrainingTypeResponse responseType = new TrainingTypeResponse("Cardio", type.getId());

        when(trainingService.listTrainingTypes()).thenReturn(List.of(type));
        when(mapper.toTrainingTypeResponse(type)).thenReturn(responseType);

        ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(responseType);
    }
}
