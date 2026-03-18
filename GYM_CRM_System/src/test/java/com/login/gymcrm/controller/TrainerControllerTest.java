package com.login.gymcrm.controller;

import com.login.gymcrm.dto.CredentialsResponse;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.dto.TrainerProfileResponse;
import com.login.gymcrm.dto.TrainerRegistrationRequest;
import com.login.gymcrm.dto.TrainerTrainingItemResponse;
import com.login.gymcrm.dto.UpdateTrainerProfileRequest;
import com.login.gymcrm.mapper.RestDtoMapper;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.service.TrainerService;
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
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private RestDtoMapper mapper;

    @InjectMocks
    private TrainerController trainerController;

    @Test
    void registerTrainerReturnsCredentials() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Coach", "Sarah", "Cardio");
        Trainer trainer = trainer("coach.sarah");
        CredentialsResponse credentials = new CredentialsResponse("coach.sarah", "pass123");

        when(trainerService.createProfile("Coach", "Sarah", "Cardio")).thenReturn(trainer);
        when(mapper.toCredentialsResponse(trainer.getUser())).thenReturn(credentials);

        ResponseEntity<CredentialsResponse> response = trainerController.registerTrainer(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(credentials);
    }

    @Test
    void getTrainerProfileReturnsMappedResponse() {
        Trainer trainer = trainer("coach.sarah");
        TrainerProfileResponse profile = new TrainerProfileResponse("coach.sarah", "Coach", "Sarah", "Cardio", true, List.of());

        when(trainerService.selectProfileByUsername("coach.sarah")).thenReturn(trainer);
        when(mapper.toTrainerProfileResponse(trainer)).thenReturn(profile);

        ResponseEntity<TrainerProfileResponse> response = trainerController.getTrainerProfile("coach.sarah");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(profile);
    }

    @Test
    void updateTrainerProfileUsesExistingSpecialization() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("coach.sarah", "Coach", "Connor", false);
        Trainer existing = trainer("coach.sarah");
        Trainer updated = trainer("coach.sarah");
        updated.setLastName("Connor");
        updated.setActive(false);

        TrainerProfileResponse profile = new TrainerProfileResponse("coach.sarah", "Coach", "Connor", "Cardio", false, List.of());

        when(trainerService.selectProfileByUsername("coach.sarah")).thenReturn(existing);
        when(trainerService.updateProfile(existing)).thenReturn(updated);
        when(mapper.toTrainerProfileResponse(updated)).thenReturn(profile);

        ResponseEntity<TrainerProfileResponse> response = trainerController.updateTrainerProfile(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(profile);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerService).updateProfile(captor.capture());
        Trainer sent = captor.getValue();
        assertThat(sent.getSpecialization()).isEqualTo("Cardio");
        assertThat(sent.getLastName()).isEqualTo("Connor");
        assertThat(sent.isActive()).isFalse();
    }

    @Test
    void getTrainerTrainingsReturnsMappedItems() {
        var item = new TrainerTrainingItemResponse("Session", LocalDate.of(2026, 1, 1), "Cardio", 60, "John Smith");

        when(trainingService.getTrainerTrainingsByCriteria("coach.sarah", null, null, null)).thenReturn(List.of(training()));
        when(mapper.toTrainerTrainingItemResponse(org.mockito.ArgumentMatchers.any())).thenReturn(item);

        ResponseEntity<List<TrainerTrainingItemResponse>> response = trainerController.getTrainerTrainings(
                "coach.sarah", null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(item);
    }

    @Test
    void toggleTrainerActivationReturnsMessage() {
        Trainer trainer = trainer("coach.sarah");
        trainer.setActive(false);
        when(trainerService.changeStateByUserId("user-2")).thenReturn(trainer);

        ResponseEntity<MessageResponse> response = trainerController.toggleTrainerActivation("user-2");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new MessageResponse("Trainer active state is now: false"));
    }

    private Trainer trainer(String username) {
        Trainer trainer = new Trainer();
        trainer.setId(UUID.randomUUID().toString());
        trainer.setUser(new User(UUID.randomUUID().toString(), "Coach", "Sarah", username, "pass", true));
        trainer.setSpecialization("Cardio");

        Trainee trainee = new Trainee();
        trainee.setId(UUID.randomUUID().toString());
        trainee.setUser(new User(UUID.randomUUID().toString(), "John", "Smith", "john.smith", "pass", true));
        trainer.getTrainees().add(trainee);
        return trainer;
    }

    private com.login.gymcrm.model.Training training() {
        return new com.login.gymcrm.model.Training(
                UUID.randomUUID().toString(),
                new Trainee(UUID.randomUUID().toString(), "John", "Smith", "john.smith", "pass", true),
                trainer("coach.sarah"),
                new com.login.gymcrm.model.TrainingType(UUID.randomUUID().toString(), "Cardio"),
                "Session",
                LocalDate.of(2026, 1, 1),
                60
        );
    }
}
