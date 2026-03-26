package com.login.gymcrm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "specialization", length = 150)
    private String specialization;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Training> trainings = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new LinkedHashSet<>();

    public Trainer() {
    }

    public Trainer(String id, String firstName, String lastName, String username, String password, String specialization) {
        this.id = UUID.fromString(id);
        this.user = new User(UUID.randomUUID().toString(), firstName, lastName, username, password, true);
        this.specialization = specialization;
    }

    public String getId() {
        return id == null ? null : id.toString();
    }

    public void setId(String id) {
        this.id = id == null ? null : UUID.fromString(id);
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Set<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(Set<Training> trainings) {
        this.trainings = trainings;
    }

    public Set<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(Set<Trainee> trainees) {
        this.trainees = trainees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Trainer trainer = (Trainer) o;
        return Objects.equals(id, trainer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "id='" + getId() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", specialization='" + specialization + '\'' +
                ", active=" + isActive() +
                '}';
    }
}
