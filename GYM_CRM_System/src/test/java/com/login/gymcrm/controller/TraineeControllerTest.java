package com.login.gymcrm.controller;

import com.login.gymcrm.dto.CredentialsResponse;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.dto.TraineeProfileResponse;
import com.login.gymcrm.dto.TraineeRegistrationRequest;
import com.login.gymcrm.dto.TraineeTrainingItemResponse;
import com.login.gymcrm.dto.TrainerSummaryResponse;
import com.login.gymcrm.dto.UpdateTraineeProfileRequest;
import com.login.gymcrm.dto.UpdateTraineeTrainersRequest;
import com.login.gymcrm.mapper.RestDtoMapper;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private RestDtoMapper mapper;

    @InjectMocks
    private TraineeController traineeController;

    @Test
    void registerTraineeReturnsCredentials() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest("John", "Smith", LocalDate.of(1999, 1, 1), "Main");
        Trainee trainee = trainee("john.smith");
        CredentialsResponse credentials = new CredentialsResponse("john.smith", "pass123");

        when(traineeService.createProfile("John", "Smith", LocalDate.of(1999, 1, 1), "Main")).thenReturn(trainee);
        when(mapper.toCredentialsResponse(trainee.getUser())).thenReturn(credentials);

        ResponseEntity<CredentialsResponse> response = traineeController.registerTrainee(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(credentials);
    }

    @Test
    void getTraineeProfileReturnsMappedResponse() {
        Trainee trainee = trainee("john.smith");
        TraineeProfileResponse profile = new TraineeProfileResponse("john.smith", "John", "Smith", null, null, true, List.of());

        when(traineeService.selectProfileByUsername("john.smith")).thenReturn(trainee);
        when(mapper.toTraineeProfileResponse(trainee)).thenReturn(profile);

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile("john.smith");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(profile);
    }

    @Test
    void updateTraineeProfileUpdatesExistingEntity() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "john.smith", "John", "Stone", LocalDate.of(2000, 2, 2), "Wall street", false);

        Trainee existing = trainee("john.smith");
        Trainee updated = trainee("john.smith");
        updated.setLastName("Stone");
        updated.setDateOfBirth(LocalDate.of(2000, 2, 2));
        updated.setAddress("Wall street");
        updated.setActive(false);

        TraineeProfileResponse profile = new TraineeProfileResponse("john.smith", "John", "Stone",
                LocalDate.of(2000, 2, 2), "Wall street", false, List.of());

        when(traineeService.selectProfileByUsername("john.smith")).thenReturn(existing);
        when(traineeService.updateProfile(existing)).thenReturn(updated);
        when(mapper.toTraineeProfileResponse(updated)).thenReturn(profile);

        ResponseEntity<TraineeProfileResponse> response = traineeController.updateTraineeProfile(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(profile);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeService).updateProfile(captor.capture());
        Trainee sent = captor.getValue();
        assertThat(sent.getFirstName()).isEqualTo("John");
        assertThat(sent.getLastName()).isEqualTo("Stone");
        assertThat(sent.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 2, 2));
        assertThat(sent.getAddress()).isEqualTo("Wall street");
        assertThat(sent.isActive()).isFalse();
    }

    @Test
    void deleteTraineeProfileReturnsOk() {
        ResponseEntity<MessageResponse> response = traineeController.deleteTraineeProfile("john.smith");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new MessageResponse("Trainee profile deleted"));
        verify(traineeService).deleteProfileByUsername("john.smith");
    }

    @Test
    void getUnassignedActiveTrainersReturnsMappedList() {
        Trainer trainer = trainer("coach.sarah", "Cardio");
        TrainerSummaryResponse summary = new TrainerSummaryResponse("coach.sarah", "Coach", "Sarah", "Cardio");

        when(traineeService.listUnassignedTrainersByTraineeUsername("john.smith")).thenReturn(List.of(trainer));
        when(mapper.toTrainerSummaryResponse(trainer)).thenReturn(summary);

        ResponseEntity<List<TrainerSummaryResponse>> response = traineeController.getUnassignedActiveTrainers("john.smith");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(summary);
    }

    @Test
    void updateTraineeTrainersListReturnsMappedList() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest("john.smith", List.of("coach.sarah"));
        Trainee trainee = trainee("john.smith");
        Trainer trainer = trainer("coach.sarah", "Cardio");
        trainee.addTrainer(trainer);

        TrainerSummaryResponse summary = new TrainerSummaryResponse("coach.sarah", "Coach", "Sarah", "Cardio");

        when(traineeService.updateTrainersListByUsernames("john.smith", List.of("coach.sarah"))).thenReturn(trainee);
        when(mapper.toTrainerSummaryResponse(trainer)).thenReturn(summary);

        ResponseEntity<List<TrainerSummaryResponse>> response = traineeController.updateTraineeTrainersList(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(summary);
    }

    @Test
    void getTraineeTrainingsReturnsMappedTrainings() {
        var item = new TraineeTrainingItemResponse("Cardio blast", LocalDate.of(2026, 1, 1), "Cardio", 45, "Coach Sarah");

        when(trainingService.getTraineeTrainingsByCriteria("john.smith", null, null, null, null)).thenReturn(List.of(training()));
        when(mapper.toTraineeTrainingItemResponse(org.mockito.ArgumentMatchers.any())).thenReturn(item);

        ResponseEntity<List<TraineeTrainingItemResponse>> response = traineeController.getTraineeTrainings(
                "john.smith", null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(item);
    }

    @Test
    void toggleTraineeActivationReturnsStateMessage() {
        Trainee trainee = trainee("john.smith");
        trainee.setActive(false);
        when(traineeService.changeStateByUserId("user-1")).thenReturn(trainee);

        ResponseEntity<MessageResponse> response = traineeController.toggleTraineeActivation("user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new MessageResponse("Trainee active state is now: false"));
    }

    private Trainee trainee(String username) {
        Trainee trainee = new Trainee();
        trainee.setId(UUID.randomUUID().toString());
        trainee.setUser(new User(UUID.randomUUID().toString(), "John", "Smith", username, "pass", true));
        return trainee;
    }

    private Trainer trainer(String username, String specialization) {
        Trainer trainer = new Trainer();
        trainer.setId(UUID.randomUUID().toString());
        trainer.setUser(new User(UUID.randomUUID().toString(), "Coach", "Sarah", username, "pass", true));
        trainer.setSpecialization(specialization);
        return trainer;
    }

    private com.login.gymcrm.model.Training training() {
        return new com.login.gymcrm.model.Training(
                UUID.randomUUID().toString(),
                trainee("john.smith"),
                trainer("coach.sarah", "Cardio"),
                new com.login.gymcrm.model.TrainingType(UUID.randomUUID().toString(), "Cardio"),
                "Cardio blast",
                LocalDate.of(2026, 1, 1),
                45
        );
    }
}
