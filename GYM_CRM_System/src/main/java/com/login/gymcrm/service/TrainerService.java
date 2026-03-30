package com.login.gymcrm.service;

import com.login.gymcrm.dao.TraineeDao;
import com.login.gymcrm.dao.TrainerDao;
import com.login.gymcrm.dao.TrainingTypeDao;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.model.TrainingType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TraineeDao traineeDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UuidGenerator idGenerator;
    private final RandomPasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final EntityValidator validator;

    public TrainerService(TrainerDao trainerDao,
                          TraineeDao traineeDao,
                          TrainingTypeDao trainingTypeDao,
                          UuidGenerator idGenerator,
                          RandomPasswordGenerator passwordGenerator,
                          UsernameGenerator usernameGenerator,
                          EntityValidator validator) {
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.trainingTypeDao = trainingTypeDao;
        this.idGenerator = idGenerator;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
        this.validator = validator;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER})
    @Transactional
    public Trainer createProfile(String firstName, String lastName, String specialization) {
        validator.requireNames(firstName, lastName);
        validator.requireValue(specialization, "Specialization is required");

        String normalizedFirstName = firstName.trim();
        String normalizedLastName = lastName.trim();
        String normalizedSpecialization = specialization.trim();

        String canonicalSpecialization = trainingTypeDao.findByName(normalizedSpecialization)
                .map(TrainingType::getName)
                .orElseThrow(() -> new ValidationException("Specialization must reference an existing training type: " + normalizedSpecialization));

        String profileId = idGenerator.generate();
        String userId = idGenerator.generate();
        String username = usernameGenerator.generate(normalizedFirstName, normalizedLastName);
        String password = passwordGenerator.generate(10);

        User user = new User(userId, normalizedFirstName, normalizedLastName, username, password, true);

        Trainer trainer = new Trainer();
        trainer.setId(profileId);
        trainer.setUser(user);
        trainer.setSpecialization(canonicalSpecialization);

        trainerDao.save(trainer);
        log.info("Created trainer profile id={} username={}", profileId, username);
        return trainer;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER})
    @Transactional
    public Trainer updateProfile(Trainer updated) {
        validator.validateTrainerForUpdate(updated);

        Trainer existing = trainerDao.findById(updated.getId())
                .orElseThrow(() -> {
                    log.warn("Trainer not found for update id={}", updated.getId());
                    return new EntityNotFoundException("Trainer not found: " + updated.getId());
                });

        existing.setFirstName(updated.getFirstName().trim());
        existing.setLastName(updated.getLastName().trim());
        existing.setActive(updated.isActive());

        trainerDao.update(existing);
        log.info("Updated trainer profile id={}", existing.getId());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER})
    @Transactional
    public Trainer changeState(String id) {
        validator.requireId(id, "Trainer id is required for state change");

        Trainer existing = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainer not found for state change id={}", id);
                    return new EntityNotFoundException("Trainer not found: " + id);
                });

        existing.setActive(!existing.isActive());
        trainerDao.update(existing);

        log.info("Changed trainer state id={} active={}", existing.getId(), existing.isActive());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER})
    @Transactional
    public Trainer changeStateByUserId(String userId) {
        validator.requireId(userId, "Trainer userId is required for state change");

        Trainer existing = trainerDao.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Trainer not found for state change by userId={}", userId);
                    return new EntityNotFoundException("Trainer not found by userId: " + userId);
                });

        existing.setActive(!existing.isActive());
        trainerDao.update(existing);

        log.info("Changed trainer state by userId={} active={}", userId, existing.isActive());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Trainer selectProfile(String id) {
        validator.requireId(id, "Trainer id is required for select");
        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainer not found for select id={}", id);
                    return new EntityNotFoundException("Trainer not found: " + id);
                });
        log.debug("Selected trainer profile id={}", id);
        return trainer;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Trainer selectProfileByUsername(String username) {
        validator.requireValue(username, "Trainer username is required for select");
        Trainer trainer = trainerDao.findByUsername(username.trim())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found by username: " + username));
        log.debug("Selected trainer profile username={}", username);
        return trainer;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Trainer> listAll() {
        List<Trainer> trainers = trainerDao.findAll();
        log.debug("Listed trainer profiles count={}", trainers.size());
        return trainers;
    }
}
