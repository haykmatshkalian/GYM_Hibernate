package com.login.gymcrm.service;

import com.login.gymcrm.dao.TrainerDao;
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

import java.util.List;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final UuidGenerator idGenerator;
    private final RandomPasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final EntityValidator validator;

    public TrainerService(TrainerDao trainerDao,
                          UuidGenerator idGenerator,
                          RandomPasswordGenerator passwordGenerator,
                          UsernameGenerator usernameGenerator,
                          EntityValidator validator) {
        this.trainerDao = trainerDao;
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

        String profileId = idGenerator.generate();
        String userId = idGenerator.generate();
        String username = usernameGenerator.generate(firstName, lastName);
        String password = passwordGenerator.generate(10);

        User user = new User(userId, firstName.trim(), lastName.trim(), username, password, true);

        Trainer trainer = new Trainer();
        trainer.setId(profileId);
        trainer.setUser(user);
        trainer.setSpecialization(specialization.trim());

        trainerDao.save(trainer);
        log.info("Created trainer profile id={} username={}", profileId, username);
        return trainer;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER})
    @Transactional
    public Trainer updateProfile(Trainer updated) {
        validator.validateTrainerForUpdate(updated);

        Trainer existing = trainerDao.findById(updated.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + updated.getId()));

        existing.setFirstName(updated.getFirstName().trim());
        existing.setLastName(updated.getLastName().trim());
        existing.setSpecialization(updated.getSpecialization().trim());
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
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + id));

        existing.setActive(!existing.isActive());
        trainerDao.update(existing);

        log.info("Changed trainer state id={} active={}", existing.getId(), existing.isActive());
        return existing;
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Trainer selectProfile(String id) {
        validator.requireId(id, "Trainer id is required for select");
        return trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + id));
    }

    @Authorized({Role.ADMIN, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Trainer> listAll() {
        return trainerDao.findAll();
    }
}
