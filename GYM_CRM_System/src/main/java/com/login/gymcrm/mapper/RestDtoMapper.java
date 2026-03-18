package com.login.gymcrm.mapper;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.model.TrainingType;
import com.login.gymcrm.model.User;
import com.login.gymcrm.dto.CredentialsResponse;
import com.login.gymcrm.dto.TraineeProfileResponse;
import com.login.gymcrm.dto.TraineeSummaryResponse;
import com.login.gymcrm.dto.TraineeTrainingItemResponse;
import com.login.gymcrm.dto.TrainerProfileResponse;
import com.login.gymcrm.dto.TrainerSummaryResponse;
import com.login.gymcrm.dto.TrainerTrainingItemResponse;
import com.login.gymcrm.dto.TrainingTypeResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RestDtoMapper {

    public CredentialsResponse toCredentialsResponse(User user) {
        return new CredentialsResponse(user.getUsername(), user.getPassword());
    }

    public TraineeProfileResponse toTraineeProfileResponse(Trainee trainee) {
        List<TrainerSummaryResponse> trainers = trainee.getTrainers().stream()
                .map(this::toTrainerSummaryResponse)
                .sorted(Comparator.comparing(TrainerSummaryResponse::username, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new TraineeProfileResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainers
        );
    }

    public TrainerProfileResponse toTrainerProfileResponse(Trainer trainer) {
        List<TraineeSummaryResponse> trainees = trainer.getTrainees().stream()
                .map(this::toTraineeSummaryResponse)
                .sorted(Comparator.comparing(TraineeSummaryResponse::username, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new TrainerProfileResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization(),
                trainer.isActive(),
                trainees
        );
    }

    public TrainerSummaryResponse toTrainerSummaryResponse(Trainer trainer) {
        return new TrainerSummaryResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization()
        );
    }

    public TraineeSummaryResponse toTraineeSummaryResponse(Trainee trainee) {
        return new TraineeSummaryResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName()
        );
    }

    public TraineeTrainingItemResponse toTraineeTrainingItemResponse(Training training) {
        return new TraineeTrainingItemResponse(
                training.getName(),
                training.getDate(),
                training.getTrainingType().getName(),
                training.getDurationMinutes(),
                training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName()
        );
    }

    public TrainerTrainingItemResponse toTrainerTrainingItemResponse(Training training) {
        return new TrainerTrainingItemResponse(
                training.getName(),
                training.getDate(),
                training.getTrainingType().getName(),
                training.getDurationMinutes(),
                training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName()
        );
    }

    public TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType) {
        return new TrainingTypeResponse(trainingType.getName(), trainingType.getId());
    }
}
