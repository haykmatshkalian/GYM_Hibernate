package com.login.gymcrm.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ModelComprehensiveTest {

    @Test
    void userGettersSettersAndEqualityWork() {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, "John", "Smith", "john.smith", "secret", true);

        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getUsername()).isEqualTo("john.smith");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.isActive()).isTrue();

        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setUsername("jane.doe");
        user.setPassword("pass");
        user.setActive(false);

        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getUsername()).isEqualTo("jane.doe");
        assertThat(user.getPassword()).isEqualTo("pass");
        assertThat(user.isActive()).isFalse();

        User sameId = new User(userId, "A", "B", "x", "y", true);
        User differentId = new User(UUID.randomUUID().toString(), "A", "B", "x", "y", true);

        assertThat(user).isEqualTo(sameId);
        assertThat(user).isNotEqualTo(differentId);
        assertThat(user.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void traineeTrainerAndTrainingAssociationsWorkBidirectionally() {
        String traineeId = UUID.randomUUID().toString();
        String trainerId = UUID.randomUUID().toString();
        String trainingTypeId = UUID.randomUUID().toString();
        String trainingId = UUID.randomUUID().toString();

        Trainee trainee = new Trainee(traineeId, "Alex", "Stone", "alex.stone", "pwd", true);
        Trainer trainer = new Trainer(trainerId, "Sarah", "Cole", "sarah.cole", "pwd", "Yoga");

        trainee.addTrainer(trainer);
        assertThat(trainee.getTrainers()).contains(trainer);
        assertThat(trainer.getTrainees()).contains(trainee);

        trainee.removeTrainer(trainer);
        assertThat(trainee.getTrainers()).doesNotContain(trainer);
        assertThat(trainer.getTrainees()).doesNotContain(trainee);

        TrainingType trainingType = new TrainingType(trainingTypeId, "Strength");
        Training training = new Training(trainingId, trainee, trainer, trainingType, "Session", LocalDate.now(), 45);

        trainee.addTraining(training);
        assertThat(trainee.getTrainings()).contains(training);
        assertThat(training.getTrainee()).isEqualTo(trainee);

        trainee.removeTraining(training);
        assertThat(trainee.getTrainings()).doesNotContain(training);
        assertThat(training.getTrainee()).isNull();
    }

    @Test
    void traineeAndTrainerDelegatedUserFieldsAndToStringWork() {
        String traineeId = UUID.randomUUID().toString();
        Trainee trainee = new Trainee();
        trainee.setId(traineeId);
        trainee.setUser(new User(UUID.randomUUID().toString(), "Mia", "Ray", "mia.ray", "pwd", true));

        trainee.setFirstName("Mila");
        trainee.setLastName("Roe");
        trainee.setUsername("mila.roe");
        trainee.setPassword("newpwd");
        trainee.setActive(false);
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Yerevan");

        assertThat(trainee.getFirstName()).isEqualTo("Mila");
        assertThat(trainee.getLastName()).isEqualTo("Roe");
        assertThat(trainee.getUsername()).isEqualTo("mila.roe");
        assertThat(trainee.getPassword()).isEqualTo("newpwd");
        assertThat(trainee.isActive()).isFalse();
        assertThat(trainee.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(trainee.getAddress()).isEqualTo("Yerevan");
        assertThat(trainee.toString()).contains(traineeId, "Mila", "Roe");

        String trainerId = UUID.randomUUID().toString();
        Trainer trainer = new Trainer();
        trainer.setId(trainerId);
        trainer.setUser(new User(UUID.randomUUID().toString(), "Leo", "King", "leo.king", "pwd", true));
        trainer.setSpecialization("Cardio");
        trainer.setTrainings(new LinkedHashSet<>());
        trainer.setTrainees(new LinkedHashSet<>());

        trainer.setFirstName("Leon");
        trainer.setLastName("Kingsley");
        trainer.setUsername("leon.kingsley");
        trainer.setPassword("pwd2");
        trainer.setActive(false);

        assertThat(trainer.getFirstName()).isEqualTo("Leon");
        assertThat(trainer.getLastName()).isEqualTo("Kingsley");
        assertThat(trainer.getUsername()).isEqualTo("leon.kingsley");
        assertThat(trainer.getPassword()).isEqualTo("pwd2");
        assertThat(trainer.isActive()).isFalse();
        assertThat(trainer.getSpecialization()).isEqualTo("Cardio");
        assertThat(trainer.toString()).contains(trainerId, "Leon", "Cardio");
    }

    @Test
    void trainingAndTrainingTypeConstructorsAndEqualityWork() {
        String typeId = UUID.randomUUID().toString();
        TrainingType type = new TrainingType(typeId, "Cardio");
        type.setName("Strength");
        assertThat(type.getName()).isEqualTo("Strength");

        TrainingType sameTypeId = new TrainingType(typeId, "Any");
        TrainingType otherType = new TrainingType(UUID.randomUUID().toString(), "Any");
        assertThat(type).isEqualTo(sameTypeId);
        assertThat(type).isNotEqualTo(otherType);

        String trainingId = UUID.randomUUID().toString();
        String traineeId = UUID.randomUUID().toString();
        String trainerId = UUID.randomUUID().toString();
        Training training = new Training(trainingId, traineeId, trainerId, "Morning", LocalDate.of(2026, 1, 1), 30);
        training.setName("Evening");
        training.setDate(LocalDate.of(2026, 1, 2));
        training.setDurationMinutes(60);
        training.setTrainingType(type);

        assertThat(training.getId()).isEqualTo(trainingId);
        assertThat(training.getTraineeId()).isEqualTo(traineeId);
        assertThat(training.getTrainerId()).isEqualTo(trainerId);
        assertThat(training.getName()).isEqualTo("Evening");
        assertThat(training.getDate()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(training.getDurationMinutes()).isEqualTo(60);
        assertThat(training.getTrainingType()).isEqualTo(type);
        assertThat(training.toString()).contains(trainingId, traineeId, trainerId, "Strength");

        Training sameId = new Training(trainingId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), "X", LocalDate.now(), 1);
        Training differentId = new Training(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), "X", LocalDate.now(), 1);
        assertThat(training).isEqualTo(sameId);
        assertThat(training).isNotEqualTo(differentId);
    }
}
