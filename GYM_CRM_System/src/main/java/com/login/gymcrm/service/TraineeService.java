package com.login.gymcrm.service;

import com.login.gymcrm.dao.TraineeDao;
import com.login.gymcrm.dao.TrainerDao;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.security.Authorized;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.exception.ValidationException;
import com.login.gymcrm.service.validator.EntityValidator;
import com.login.gymcrm.util.RandomPasswordGenerator;
import com.login.gymcrm.util.UsernameGenerator;
import com.login.gymcrm.util.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UuidGenerator idGenerator;
    private final UsernameGenerator usernameGenerator;
    private final RandomPasswordGenerator passwordGenerator;
    private final EntityValidator validator;
    private final PasswordEncoder passwordEncoder;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          UuidGenerator idGenerator,
                          UsernameGenerator usernameGenerator,
                          RandomPasswordGenerator passwordGenerator,
                          EntityValidator validator,
                          PasswordEncoder passwordEncoder) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.idGenerator = idGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee createProfile(String firstName, String lastName) {
        return createProfile(firstName, lastName, null, null);
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        validator.requireNames(firstName, lastName);

        String normalizedFirstName = firstName.trim();
        String normalizedLastName = lastName.trim();

        String profileId = idGenerator.generate();
        String userId = idGenerator.generate();
        String username = usernameGenerator.generate(normalizedFirstName, normalizedLastName);
        String generatedPassword = passwordGenerator.generate(10);

        User user = new User(
                userId,
                normalizedFirstName,
                normalizedLastName,
                username,
                passwordEncoder.encode(generatedPassword),
                true
        );
        user.setGeneratedPassword(generatedPassword);

        Trainee trainee = new Trainee();
        trainee.setId(profileId);
        trainee.setUser(user);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address == null || address.isBlank() ? null : address.trim());

        traineeDao.save(trainee);
        log.info("Created trainee profile id={} username={}", profileId, username);
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee updateProfile(Trainee updated) {
        validator.validateTraineeForUpdate(updated);

        Trainee existing = traineeDao.findById(updated.getId())
                .orElseThrow(() -> {
                    log.warn("Trainee not found for update id={}", updated.getId());
                    return new EntityNotFoundException("Trainee not found: " + updated.getId());
                });

        existing.setFirstName(updated.getFirstName().trim());
        existing.setLastName(updated.getLastName().trim());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setAddress(updated.getAddress() == null ? null : updated.getAddress().trim());
        existing.setActive(updated.isActive());

        traineeDao.update(existing);
        log.info("Updated trainee profile id={}", existing.getId());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee changeState(String id) {
        validator.requireId(id, "Trainee id is required for state change");

        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainee not found for state change id={}", id);
                    return new EntityNotFoundException("Trainee not found: " + id);
                });

        existing.setActive(!existing.isActive());
        traineeDao.update(existing);

        log.info("Changed trainee state id={} active={}", existing.getId(), existing.isActive());
        return existing;
    }

    @Transactional
    public Trainee changeStateByUserId(String userId) {
        validator.requireId(userId, "Trainee userId is required for state change");

        Trainee existing = traineeDao.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Trainee not found for state change by userId={}", userId);
                    return new EntityNotFoundException("Trainee not found by userId: " + userId);
                });

        existing.setActive(!existing.isActive());
        traineeDao.update(existing);

        log.info("Changed trainee state by userId={} active={}", userId, existing.isActive());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee updateTrainersList(String traineeId, List<String> trainerIds) {
        validator.requireId(traineeId, "Trainee id is required for trainer list update");
        validator.validateTrainerIds(trainerIds);

        Trainee trainee = traineeDao.findById(traineeId)
                .orElseThrow(() -> {
                    log.warn("Trainee not found for trainers list update id={}", traineeId);
                    return new EntityNotFoundException("Trainee not found: " + traineeId);
                });

        Set<Trainer> newTrainers = new LinkedHashSet<>();
        for (String trainerId : trainerIds) {
            Trainer trainer = trainerDao.findById(trainerId)
                    .orElseThrow(() -> {
                        log.warn("Trainer not found while updating trainee trainers list traineeId={} trainerId={}", traineeId, trainerId);
                        return new EntityNotFoundException("Trainer not found: " + trainerId);
                    });
            newTrainers.add(trainer);
        }

        replaceTrainers(trainee, newTrainers);
        traineeDao.update(trainee);
        log.info("Updated trainee trainers list traineeId={} trainersCount={}", traineeId, newTrainers.size());
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee updateTrainersListByUsernames(String traineeUsername, List<String> trainerUsernames) {
        validator.requireValue(traineeUsername, "Trainee username is required");
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            throw new ValidationException("At least one trainer username is required");
        }

        Trainee trainee = traineeDao.findByUsername(traineeUsername.trim())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found by username: " + traineeUsername));

        Set<Trainer> newTrainers = new LinkedHashSet<>();
        for (String trainerUsername : trainerUsernames) {
            validator.requireValue(trainerUsername, "Trainer username is required");
            Trainer trainer = trainerDao.findByUsername(trainerUsername.trim())
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found by username: " + trainerUsername));
            newTrainers.add(trainer);
        }

        replaceTrainers(trainee, newTrainers);
        traineeDao.update(trainee);
        log.info("Updated trainee trainers list traineeUsername={} trainersCount={}", traineeUsername, newTrainers.size());
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Trainer> listUnassignedTrainersByTraineeUsername(String traineeUsername) {
        validator.requireValue(traineeUsername, "Trainee username is required");

        Trainee trainee = traineeDao.findByUsername(traineeUsername.trim())
                .orElseThrow(() -> {
                    log.warn("Trainee not found by username for unassigned trainers lookup username={}", traineeUsername);
                    return new EntityNotFoundException("Trainee not found by username: " + traineeUsername);
                });

        Set<String> assignedTrainerIds = trainee.getTrainers().stream()
                .map(Trainer::getId)
                .collect(Collectors.toSet());

        List<Trainer> unassigned = trainerDao.findAll().stream()
                .filter(Trainer::isActive)
                .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
                .toList();

        log.debug("Listed unassigned trainers for trainee username={} count={}", traineeUsername, unassigned.size());
        return unassigned;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public void deleteProfile(String id) {
        validator.requireId(id, "Trainee id is required for delete");

        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainee not found for delete id={}", id);
                    return new EntityNotFoundException("Trainee not found: " + id);
                });

        traineeDao.deleteById(existing.getId());
        log.info("Deleted trainee profile id={}", id);
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public void deleteProfileByUsername(String username) {
        validator.requireValue(username, "Trainee username is required for delete");
        Trainee trainee = traineeDao.findByUsername(username.trim())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found by username: " + username));
        traineeDao.deleteById(trainee.getId());
        log.info("Deleted trainee profile username={} id={}", username, trainee.getId());
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Trainee selectProfile(String id) {
        validator.requireId(id, "Trainee id is required for select");
        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainee not found for select id={}", id);
                    return new EntityNotFoundException("Trainee not found: " + id);
                });
        log.debug("Selected trainee profile id={}", id);
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Trainee selectProfileByUsername(String username) {
        validator.requireValue(username, "Trainee username is required for select");
        Trainee trainee = traineeDao.findByUsername(username.trim())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found by username: " + username));
        log.debug("Selected trainee profile username={}", username);
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Trainee> listAll() {
        List<Trainee> trainees = traineeDao.findAll();
        log.debug("Listed trainee profiles count={}", trainees.size());
        return trainees;
    }

    private void replaceTrainers(Trainee trainee, Set<Trainer> newTrainers) {
        Set<Trainer> existing = new LinkedHashSet<>(trainee.getTrainers());
        existing.forEach(trainee::removeTrainer);
        newTrainers.forEach(trainee::addTrainer);
    }
}
