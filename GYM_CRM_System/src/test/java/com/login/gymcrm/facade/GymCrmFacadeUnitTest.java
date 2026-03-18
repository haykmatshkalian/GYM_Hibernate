package com.login.gymcrm.facade;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.security.SecurityRoleContext;
import com.login.gymcrm.service.TraineeService;
import com.login.gymcrm.service.TrainerService;
import com.login.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GymCrmFacadeUnitTest {

    @Test
    void facadeDelegatesAllOperationsToServicesAndSecurityContext() {
        FakeTraineeService traineeService = new FakeTraineeService();
        FakeTrainerService trainerService = new FakeTrainerService();
        FakeTrainingService trainingService = new FakeTrainingService();
        SecurityRoleContext securityRoleContext = new SecurityRoleContext("ADMIN");

        GymCrmFacade facade = new GymCrmFacade(traineeService, trainerService, trainingService, securityRoleContext);

        String traineeId = UUID.randomUUID().toString();
        String trainerId = UUID.randomUUID().toString();
        String trainingId = UUID.randomUUID().toString();

        Trainee trainee = new Trainee(traineeId, "A", "B", "a.b", "p", true);
        Trainer trainer = new Trainer(trainerId, "C", "D", "c.d", "p", "Cardio");
        Training training = new Training(trainingId, traineeId, trainerId, "Session", LocalDate.of(2026, 1, 1), 30);

        traineeService.returnTrainee = trainee;
        traineeService.returnTrainees = List.of(trainee);
        traineeService.returnUnassignedTrainers = List.of(trainer);
        trainerService.returnTrainer = trainer;
        trainerService.returnTrainers = List.of(trainer);
        trainingService.returnTraining = training;
        trainingService.returnTrainings = List.of(training);
        trainingService.returnTraineeCriteriaTrainings = List.of(training);
        trainingService.returnTrainerCriteriaTrainings = List.of(training);

        facade.setCurrentRole(Role.TRAINEE_MANAGER);
        assertThat(securityRoleContext.getCurrentRole()).isEqualTo(Role.TRAINEE_MANAGER);
        facade.clearCurrentRole();
        assertThat(securityRoleContext.getCurrentRole()).isEqualTo(Role.ADMIN);

        assertThat(facade.createTrainee("A", "B")).isSameAs(trainee);
        assertThat(facade.updateTrainee(trainee)).isSameAs(trainee);
        assertThat(facade.changeTraineeState(traineeId)).isSameAs(trainee);
        assertThat(facade.updateTraineeTrainers(traineeId, List.of(trainerId))).isSameAs(trainee);
        assertThat(facade.listUnassignedTrainersByTraineeUsername(trainee.getUsername())).containsExactly(trainer);
        facade.deleteTrainee(traineeId);
        assertThat(facade.selectTrainee(traineeId)).isSameAs(trainee);
        assertThat(facade.listTrainees()).containsExactly(trainee);

        assertThat(traineeService.deletedIds).containsExactly(traineeId);

        assertThat(facade.createTrainer("C", "D", "Cardio")).isSameAs(trainer);
        assertThat(facade.updateTrainer(trainer)).isSameAs(trainer);
        assertThat(facade.changeTrainerState(trainerId)).isSameAs(trainer);
        assertThat(facade.selectTrainer(trainerId)).isSameAs(trainer);
        assertThat(facade.listTrainers()).containsExactly(trainer);

        assertThat(facade.createTraining(traineeId, trainerId, "Session", LocalDate.of(2026, 1, 1), 30)).isSameAs(training);
        assertThat(facade.selectTraining(trainingId)).isSameAs(training);
        assertThat(facade.listTrainings()).containsExactly(training);
        assertThat(facade.listTraineeTrainingsByCriteria(trainee.getUsername(), null, null, null, null)).containsExactly(training);
        assertThat(facade.listTrainerTrainingsByCriteria(trainer.getUsername(), null, null, null)).containsExactly(training);
    }

    private static final class FakeTraineeService extends TraineeService {
        private Trainee returnTrainee;
        private List<Trainee> returnTrainees = List.of();
        private List<Trainer> returnUnassignedTrainers = List.of();
        private final List<String> deletedIds = new ArrayList<>();

        private FakeTraineeService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public Trainee createProfile(String firstName, String lastName) {
            return returnTrainee;
        }

        @Override
        public Trainee updateProfile(Trainee updated) {
            return returnTrainee;
        }

        @Override
        public Trainee changeState(String id) {
            return returnTrainee;
        }

        @Override
        public Trainee updateTrainersList(String traineeId, List<String> trainerIds) {
            return returnTrainee;
        }

        @Override
        public List<Trainer> listUnassignedTrainersByTraineeUsername(String traineeUsername) {
            return returnUnassignedTrainers;
        }

        @Override
        public void deleteProfile(String id) {
            deletedIds.add(id);
        }

        @Override
        public Trainee selectProfile(String id) {
            return returnTrainee;
        }

        @Override
        public List<Trainee> listAll() {
            return returnTrainees;
        }
    }

    private static final class FakeTrainerService extends TrainerService {
        private Trainer returnTrainer;
        private List<Trainer> returnTrainers = List.of();

        private FakeTrainerService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public Trainer createProfile(String firstName, String lastName, String specialization) {
            return returnTrainer;
        }

        @Override
        public Trainer updateProfile(Trainer updated) {
            return returnTrainer;
        }

        @Override
        public Trainer changeState(String id) {
            return returnTrainer;
        }

        @Override
        public Trainer selectProfile(String id) {
            return returnTrainer;
        }

        @Override
        public List<Trainer> listAll() {
            return returnTrainers;
        }
    }

    private static final class FakeTrainingService extends TrainingService {
        private Training returnTraining;
        private List<Training> returnTrainings = List.of();
        private List<Training> returnTraineeCriteriaTrainings = List.of();
        private List<Training> returnTrainerCriteriaTrainings = List.of();

        private FakeTrainingService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public Training createTraining(String traineeId, String trainerId, String name, LocalDate date, int durationMinutes) {
            return returnTraining;
        }

        @Override
        public List<Training> getTraineeTrainingsByCriteria(String traineeUsername,
                                                            LocalDate fromDate,
                                                            LocalDate toDate,
                                                            String trainerName,
                                                            String trainingTypeName) {
            return returnTraineeCriteriaTrainings;
        }

        @Override
        public List<Training> getTrainerTrainingsByCriteria(String trainerUsername,
                                                            LocalDate fromDate,
                                                            LocalDate toDate,
                                                            String traineeName) {
            return returnTrainerCriteriaTrainings;
        }

        @Override
        public Training selectTraining(String id) {
            return returnTraining;
        }

        @Override
        public List<Training> listAll() {
            return returnTrainings;
        }
    }
}
