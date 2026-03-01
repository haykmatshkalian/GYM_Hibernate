package com.login.gymcrm.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    @Test
    void oneToOneAndManyToManyRelationshipsAreMaintainedInMemory() {
        Trainee trainee = new Trainee("t1", "Alex", "Jones", "Alex.Jones", "pass", true);
        Trainer trainer = new Trainer("r1", "Sarah", "Cole", "Sarah.Cole", "pass", "Yoga");

        trainee.addTrainer(trainer);

        assertThat(trainee.getUser()).isNotNull();
        assertThat(trainer.getUser()).isNotNull();
        assertThat(trainee.getTrainers()).contains(trainer);
        assertThat(trainer.getTrainees()).contains(trainee);
    }
}
