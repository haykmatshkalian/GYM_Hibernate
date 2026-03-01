package com.login.gymcrm.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;

class ModelComprehensiveTest {

    @Test
    void userGettersSettersAndEqualityWork() {
        User user = new User("u1", "John", "Smith", "john.smith", "secret", true);

        assertThat(user.getId()).isEqualTo("u1");
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

        User sameId = new User("u1", "A", "B", "x", "y", true);
        User differentId = new User("u2", "A", "B", "x", "y", true);

        assertThat(user).isEqualTo(sameId);
        assertThat(user).isNotEqualTo(differentId);
        assertThat(user.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void traineeTrainerAndTrainingAssociationsWorkBidirectionally() {
        Trainee trainee = new Trainee("t1", "Alex", "Stone", "alex.stone", "pwd", true);
        Trainer trainer = new Trainer("r1", "Sarah", "Cole", "sarah.cole", "pwd", "Yoga");

        trainee.addTrainer(trainer);
        assertThat(trainee.getTrainers()).contains(trainer);
        assertThat(trainer.getTrainees()).contains(trainee);

        trainee.removeTrainer(trainer);
        assertThat(trainee.getTrainers()).doesNotContain(trainer);
        assertThat(trainer.getTrainees()).doesNotContain(trainee);

        TrainingType trainingType = new TrainingType("tt1", "Strength");
        Training training = new Training("tr1", trainee, trainer, trainingType, "Session", LocalDate.now(), 45);

        trainee.addTraining(training);
        assertThat(trainee.getTrainings()).contains(training);
        assertThat(training.getTrainee()).isEqualTo(trainee);

        trainee.removeTraining(training);
        assertThat(trainee.getTrainings()).doesNotContain(training);
        assertThat(training.getTrainee()).isNull();
    }

    @Test
    void traineeAndTrainerDelegatedUserFieldsAndToStringWork() {
        Trainee trainee = new Trainee();
        trainee.setId("t2");
        trainee.setUser(new User("u2", "Mia", "Ray", "mia.ray", "pwd", true));

        trainee.setFirstName("Mila");
        trainee.setLastName("Roe");
        trainee.setUsername("mila.roe");
        trainee.setPassword("newpwd");
        trainee.setActive(false);

        assertThat(trainee.getFirstName()).isEqualTo("Mila");
        assertThat(trainee.getLastName()).isEqualTo("Roe");
        assertThat(trainee.getUsername()).isEqualTo("mila.roe");
        assertThat(trainee.getPassword()).isEqualTo("newpwd");
        assertThat(trainee.isActive()).isFalse();
        assertThat(trainee.toString()).contains("t2", "Mila", "Roe");

        Trainer trainer = new Trainer();
        trainer.setId("r2");
        trainer.setUser(new User("u3", "Leo", "King", "leo.king", "pwd", true));
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
        assertThat(trainer.toString()).contains("r2", "Leon", "Cardio");
    }

    @Test
    void trainingAndTrainingTypeConstructorsAndEqualityWork() {
        TrainingType type = new TrainingType("type1", "Cardio");
        type.setName("Strength");
        assertThat(type.getName()).isEqualTo("Strength");

        TrainingType sameTypeId = new TrainingType("type1", "Any");
        TrainingType otherType = new TrainingType("type2", "Any");
        assertThat(type).isEqualTo(sameTypeId);
        assertThat(type).isNotEqualTo(otherType);

        Training training = new Training("tr2", "t10", "r10", "Morning", LocalDate.of(2026, 1, 1), 30);
        training.setName("Evening");
        training.setDate(LocalDate.of(2026, 1, 2));
        training.setDurationMinutes(60);
        training.setTrainingType(type);

        assertThat(training.getId()).isEqualTo("tr2");
        assertThat(training.getTraineeId()).isEqualTo("t10");
        assertThat(training.getTrainerId()).isEqualTo("r10");
        assertThat(training.getName()).isEqualTo("Evening");
        assertThat(training.getDate()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(training.getDurationMinutes()).isEqualTo(60);
        assertThat(training.getTrainingType()).isEqualTo(type);
        assertThat(training.toString()).contains("tr2", "t10", "r10", "Strength");

        Training sameId = new Training("tr2", "tX", "rX", "X", LocalDate.now(), 1);
        Training differentId = new Training("tr3", "tX", "rX", "X", LocalDate.now(), 1);
        assertThat(training).isEqualTo(sameId);
        assertThat(training).isNotEqualTo(differentId);
    }
}
