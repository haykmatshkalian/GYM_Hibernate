package com.login.gymcrm.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    @Test
    void oneToOneAndManyToManyRelationshipsAreMaintainedInMemory() {
        Trainee trainee = new Trainee(UUID.randomUUID().toString(), "Alex", "Jones", "Alex.Jones", "pass", true);
        Trainer trainer = new Trainer(UUID.randomUUID().toString(), "Sarah", "Cole", "Sarah.Cole", "pass", "Yoga");

        trainee.addTrainer(trainer);

        assertThat(trainee.getUser()).isNotNull();
        assertThat(trainer.getUser()).isNotNull();
        assertThat(trainee.getTrainers()).contains(trainer);
        assertThat(trainer.getTrainees()).contains(trainee);
    }
}
