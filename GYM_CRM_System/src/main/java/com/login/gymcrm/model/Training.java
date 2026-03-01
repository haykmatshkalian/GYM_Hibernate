package com.login.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "training_date", nullable = false)
    private LocalDate date;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    public Training() {
    }

    public Training(String id, Trainee trainee, Trainer trainer, TrainingType trainingType,
                    String name, LocalDate date, int durationMinutes) {
        this.id = id;
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingType = trainingType;
        this.name = name;
        this.date = date;
        this.durationMinutes = durationMinutes;
    }

    public Training(String id, String traineeId, String trainerId, String name, LocalDate date, int durationMinutes) {
        this.id = id;
        this.trainee = new Trainee();
        this.trainee.setId(traineeId);
        this.trainer = new Trainer();
        this.trainer.setId(trainerId);
        this.name = name;
        this.date = date;
        this.durationMinutes = durationMinutes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public void setTrainee(Trainee trainee) {
        this.trainee = trainee;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public String getTraineeId() {
        return trainee == null ? null : trainee.getId();
    }

    public String getTrainerId() {
        return trainer == null ? null : trainer.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Training training = (Training) o;
        return Objects.equals(id, training.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Training{" +
                "id='" + id + '\'' +
                ", traineeId='" + getTraineeId() + '\'' +
                ", trainerId='" + getTrainerId() + '\'' +
                ", name='" + name + '\'' +
                ", type='" + (trainingType == null ? null : trainingType.getName()) + '\'' +
                ", date=" + date +
                ", durationMinutes=" + durationMinutes +
                '}';
    }
}
