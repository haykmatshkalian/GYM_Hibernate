package com.login.gymcrm.service;

import com.login.gymcrm.dao.TraineeDao;
import com.login.gymcrm.dao.TrainerDao;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.security.Authorized;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.validator.EntityValidator;
import com.login.gymcrm.util.RandomPasswordGenerator;
import com.login.gymcrm.util.UsernameGenerator;
import com.login.gymcrm.util.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UuidGenerator idGenerator;
    private final UsernameGenerator usernameGenerator;
    private final RandomPasswordGenerator passwordGenerator;
    private final EntityValidator validator;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          UuidGenerator idGenerator,
                          UsernameGenerator usernameGenerator,
                          RandomPasswordGenerator passwordGenerator,
                          EntityValidator validator) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.idGenerator = idGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.validator = validator;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee createProfile(String firstName, String lastName) {
        validator.requireNames(firstName, lastName);

        String profileId = idGenerator.generate();
        String userId = idGenerator.generate();
        String username = usernameGenerator.generate(firstName, lastName);
        String password = passwordGenerator.generate(10);

        User user = new User(userId, firstName.trim(), lastName.trim(), username, password, true);

        Trainee trainee = new Trainee();
        trainee.setId(profileId);
        trainee.setUser(user);

        traineeDao.save(trainee);
        log.info("Created trainee profile id={} username={}", profileId, username);
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee updateProfile(Trainee updated) {
        validator.validateTraineeForUpdate(updated);

        Trainee existing = traineeDao.findById(updated.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + updated.getId()));

        existing.setFirstName(updated.getFirstName().trim());
        existing.setLastName(updated.getLastName().trim());
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
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + id));

        existing.setActive(!existing.isActive());
        traineeDao.update(existing);

        log.info("Changed trainee state id={} active={}", existing.getId(), existing.isActive());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public Trainee updateTrainersList(String traineeId, List<String> trainerIds) {
        validator.requireId(traineeId, "Trainee id is required for trainer list update");
        validator.validateTrainerIds(trainerIds);

        Trainee trainee = traineeDao.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeId));

        Set<Trainer> newTrainers = new LinkedHashSet<>();
        for (String trainerId : trainerIds) {
            Trainer trainer = trainerDao.findById(trainerId)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerId));
            newTrainers.add(trainer);
        }

        Set<Trainer> existing = new LinkedHashSet<>(trainee.getTrainers());
        existing.forEach(trainee::removeTrainer);
        newTrainers.forEach(trainee::addTrainer);

        traineeDao.update(trainee);
        log.info("Updated trainee trainers list traineeId={} trainersCount={}", traineeId, newTrainers.size());
        return trainee;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER})
    @Transactional
    public void deleteProfile(String id) {
        validator.requireId(id, "Trainee id is required for delete");

        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + id));

        traineeDao.deleteById(existing.getId());
        log.info("Deleted trainee profile id={}", id);
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Trainee selectProfile(String id) {
        validator.requireId(id, "Trainee id is required for select");
        return traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + id));
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Trainee> listAll() {
        return traineeDao.findAll();
    }
}
