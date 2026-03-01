package com.login.gymcrm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Training> trainings = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "trainee_trainers",
            joinColumns = @JoinColumn(name = "trainee_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "trainer_id", nullable = false))
    private Set<Trainer> trainers = new LinkedHashSet<>();

    public Trainee() {
    }

    public Trainee(String id, String firstName, String lastName, String username, String password, boolean active) {
        this.id = id;
        this.user = new User(id + "-user", firstName, lastName, username, password, active);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFirstName() {
        return user == null ? null : user.getFirstName();
    }

    public void setFirstName(String firstName) {
        if (user != null) {
            user.setFirstName(firstName);
        }
    }

    public String getLastName() {
        return user == null ? null : user.getLastName();
    }

    public void setLastName(String lastName) {
        if (user != null) {
            user.setLastName(lastName);
        }
    }

    public String getUsername() {
        return user == null ? null : user.getUsername();
    }

    public void setUsername(String username) {
        if (user != null) {
            user.setUsername(username);
        }
    }

    public String getPassword() {
        return user == null ? null : user.getPassword();
    }

    public void setPassword(String password) {
        if (user != null) {
            user.setPassword(password);
        }
    }

    public boolean isActive() {
        return user != null && user.isActive();
    }

    public void setActive(boolean active) {
        if (user != null) {
            user.setActive(active);
        }
    }

    public Set<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(Set<Training> trainings) {
        this.trainings = trainings;
    }

    public Set<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(Set<Trainer> trainers) {
        this.trainers = trainers;
    }

    public void addTrainer(Trainer trainer) {
        trainers.add(trainer);
        trainer.getTrainees().add(this);
    }

    public void removeTrainer(Trainer trainer) {
        trainers.remove(trainer);
        trainer.getTrainees().remove(this);
    }

    public void addTraining(Training training) {
        trainings.add(training);
        training.setTrainee(this);
    }

    public void removeTraining(Training training) {
        trainings.remove(training);
        training.setTrainee(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Trainee trainee = (Trainee) o;
        return Objects.equals(id, trainee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "id='" + id + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", active=" + isActive() +
                '}';
    }
}
