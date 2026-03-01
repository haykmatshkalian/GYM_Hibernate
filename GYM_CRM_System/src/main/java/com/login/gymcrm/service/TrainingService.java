package com.login.gymcrm.service;

import com.login.gymcrm.dao.TraineeDao;
import com.login.gymcrm.dao.TrainerDao;
import com.login.gymcrm.dao.TrainingDao;
import com.login.gymcrm.dao.TrainingTypeDao;
import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.Training;
import com.login.gymcrm.model.TrainingType;
import com.login.gymcrm.security.Authorized;
import com.login.gymcrm.security.Role;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.validator.EntityValidator;
import com.login.gymcrm.util.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UuidGenerator idGenerator;
    private final EntityValidator validator;

    public TrainingService(TrainingDao trainingDao,
                           TraineeDao traineeDao,
                           TrainerDao trainerDao,
                           TrainingTypeDao trainingTypeDao,
                           UuidGenerator idGenerator,
                           EntityValidator validator) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.idGenerator = idGenerator;
        this.validator = validator;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER})
    @Transactional
    public Training createTraining(String traineeId, String trainerId, String name, LocalDate date, int durationMinutes) {
        validator.requireId(traineeId, "Trainee id is required");
        validator.requireId(trainerId, "Trainer id is required");
        validator.requireValue(name, "Training name is required");
        validator.requireDate(date, "Training date is required");
        validator.requirePositive(durationMinutes, "Duration must be positive");

        Trainee trainee = traineeDao.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeId));
        Trainer trainer = trainerDao.findById(trainerId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerId));

        TrainingType trainingType = resolveTrainingType(trainer.getSpecialization());

        Training training = new Training(
                idGenerator.generate(),
                trainee,
                trainer,
                trainingType,
                name.trim(),
                date,
                durationMinutes
        );

        trainee.addTrainer(trainer);
        trainee.addTraining(training);

        trainingDao.save(training);
        log.info("Created training id={} traineeId={} trainerId={} trainingType={}",
                training.getId(), traineeId, trainerId, trainingType.getName());
        return training;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Training selectTraining(String id) {
        validator.requireId(id, "Training id is required for select");
        return trainingDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found: " + id));
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Training> listAll() {
        return trainingDao.findAll();
    }

    private TrainingType resolveTrainingType(String specialization) {
        String typeName = specialization == null || specialization.isBlank()
                ? "General"
                : specialization.trim();

        return trainingTypeDao.findByName(typeName)
                .orElseGet(() -> {
                    TrainingType type = new TrainingType(idGenerator.generate(), typeName);
                    trainingTypeDao.save(type);
                    return type;
                });
    }
}
