package com.login.gymcrm.facade;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.security.SecurityRoleContext;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainerService;
import com.login.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymCrmFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final SecurityRoleContext securityRoleContext;

    public GymCrmFacade(TraineeService traineeService,
                        TrainerService trainerService,
                        TrainingService trainingService,
                        SecurityRoleContext securityRoleContext) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.securityRoleContext = securityRoleContext;
    }

    public void setCurrentRole(Role role) {
        securityRoleContext.setCurrentRole(role);
    }

    public void clearCurrentRole() {
        securityRoleContext.clear();
    }

    public Trainee createTrainee(String firstName, String lastName) {
        return traineeService.createProfile(firstName, lastName);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.updateProfile(trainee);
    }

    public Trainee changeTraineeState(String id) {
        return traineeService.changeState(id);
    }

    public Trainee updateTraineeTrainers(String traineeId, List<String> trainerIds) {
        return traineeService.updateTrainersList(traineeId, trainerIds);
    }

    public List<Trainer> listUnassignedTrainersByTraineeUsername(String traineeUsername) {
        return traineeService.listUnassignedTrainersByTraineeUsername(traineeUsername);
    }

    public void deleteTrainee(String id) {
        traineeService.deleteProfile(id);
    }

    public Trainee selectTrainee(String id) {
        return traineeService.selectProfile(id);
    }

    public List<Trainee> listTrainees() {
        return traineeService.listAll();
    }

    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        return trainerService.createProfile(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerService.updateProfile(trainer);
    }

    public Trainer changeTrainerState(String id) {
        return trainerService.changeState(id);
    }

    public Trainer selectTrainer(String id) {
        return trainerService.selectProfile(id);
    }

    public List<Trainer> listTrainers() {
        return trainerService.listAll();
    }

    public Training createTraining(String traineeId, String trainerId, String name, LocalDate date, int durationMinutes) {
        return trainingService.createTraining(traineeId, trainerId, name, date, durationMinutes);
    }

    public List<Training> listTraineeTrainingsByCriteria(String traineeUsername,
                                                         LocalDate fromDate,
                                                         LocalDate toDate,
                                                         String trainerName,
                                                         String trainingTypeName) {
        return trainingService.getTraineeTrainingsByCriteria(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> listTrainerTrainingsByCriteria(String trainerUsername,
                                                         LocalDate fromDate,
                                                         LocalDate toDate,
                                                         String traineeName) {
        return trainingService.getTrainerTrainingsByCriteria(trainerUsername, fromDate, toDate, traineeName);
    }

    public Training selectTraining(String id) {
        return trainingService.selectTraining(id);
    }

    public List<Training> listTrainings() {
        return trainingService.listAll();
    }
}
