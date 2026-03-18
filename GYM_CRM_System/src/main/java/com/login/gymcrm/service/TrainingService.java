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
import com.login.gymcrm.service.exception.ValidationException;
import com.login.gymcrm.service.validator.EntityValidator;
import com.login.gymcrm.util.UuidGenerator;
import org.apache.commons.lang3.StringUtils;
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
                .orElseThrow(() -> {
                    log.warn("Trainee not found for training create traineeId={} trainerId={}", traineeId, trainerId);
                    return new EntityNotFoundException("Trainee not found: " + traineeId);
                });
        Trainer trainer = trainerDao.findById(trainerId)
                .orElseThrow(() -> {
                    log.warn("Trainer not found for training create traineeId={} trainerId={}", traineeId, trainerId);
                    return new EntityNotFoundException("Trainer not found: " + trainerId);
                });

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

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER})
    @Transactional
    public Training createTrainingByUsernames(String traineeUsername,
                                              String trainerUsername,
                                              String name,
                                              LocalDate date,
                                              int durationMinutes) {
        validator.requireValue(traineeUsername, "Trainee username is required");
        validator.requireValue(trainerUsername, "Trainer username is required");

        Trainee trainee = traineeDao.findByUsername(traineeUsername.trim())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found by username: " + traineeUsername));
        Trainer trainer = trainerDao.findByUsername(trainerUsername.trim())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found by username: " + trainerUsername));

        return createTraining(trainee.getId(), trainer.getId(), name, date, durationMinutes);
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsByCriteria(String traineeUsername,
                                                        LocalDate fromDate,
                                                        LocalDate toDate,
                                                        String trainerName,
                                                        String trainingTypeName) {
        validator.requireValue(traineeUsername, "Trainee username is required");
        validator.validateDateRange(fromDate, toDate, "Invalid date range: from date is after to date");

        Trainee trainee = traineeDao.findByUsername(traineeUsername.trim())
                .orElseThrow(() -> {
                    log.warn("Trainee not found by username for trainings lookup username={}", traineeUsername);
                    return new EntityNotFoundException("Trainee not found by username: " + traineeUsername);
                });

        String normalizedTrainerName = normalize(trainerName);
        String normalizedTrainingType = normalize(trainingTypeName);

        List<Training> trainings = trainingDao.findByTraineeUsername(trainee.getUsername()).stream()
                .filter(training -> fromDate == null || !training.getDate().isBefore(fromDate))
                .filter(training -> toDate == null || !training.getDate().isAfter(toDate))
                .filter(training -> matchesTrainerName(training, normalizedTrainerName))
                .filter(training -> matchesTrainingType(training, normalizedTrainingType))
                .toList();

        log.debug("Listed trainee trainings by criteria username={} count={}", traineeUsername, trainings.size());
        return trainings;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsByCriteria(String trainerUsername,
                                                        LocalDate fromDate,
                                                        LocalDate toDate,
                                                        String traineeName) {
        validator.requireValue(trainerUsername, "Trainer username is required");
        validator.validateDateRange(fromDate, toDate, "Invalid date range: from date is after to date");

        Trainer trainer = trainerDao.findByUsername(trainerUsername.trim())
                .orElseThrow(() -> {
                    log.warn("Trainer not found by username for trainings lookup username={}", trainerUsername);
                    return new EntityNotFoundException("Trainer not found by username: " + trainerUsername);
                });

        String normalizedTraineeName = normalize(traineeName);

        List<Training> trainings = trainingDao.findByTrainerUsername(trainer.getUsername()).stream()
                .filter(training -> fromDate == null || !training.getDate().isBefore(fromDate))
                .filter(training -> toDate == null || !training.getDate().isAfter(toDate))
                .filter(training -> matchesTraineeName(training, normalizedTraineeName))
                .toList();

        log.debug("Listed trainer trainings by criteria username={} count={}", trainerUsername, trainings.size());
        return trainings;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<TrainingType> listTrainingTypes() {
        List<TrainingType> types = trainingTypeDao.findAll();
        log.debug("Listed training types count={}", types.size());
        return types;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public Training selectTraining(String id) {
        validator.requireId(id, "Training id is required for select");
        Training training = trainingDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Training not found for select id={}", id);
                    return new EntityNotFoundException("Training not found: " + id);
                });
        log.debug("Selected training id={}", id);
        return training;
    }

    @Authorized({Role.ADMIN, Role.TRAINEE_MANAGER, Role.TRAINER_MANAGER, Role.VIEWER})
    @Transactional(readOnly = true)
    public List<Training> listAll() {
        List<Training> trainings = trainingDao.findAll();
        log.debug("Listed trainings count={}", trainings.size());
        return trainings;
    }

    private TrainingType resolveTrainingType(String specialization) {
        String typeName = specialization == null || specialization.isBlank()
                ? "General"
                : specialization.trim();

        return trainingTypeDao.findByName(typeName)
                .orElseThrow(() -> new ValidationException("Training type is not supported: " + typeName));
    }

    private boolean matchesTrainerName(Training training, String normalizedTrainerName) {
        if (normalizedTrainerName == null) {
            return true;
        }
        String fullName = (training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName()).toLowerCase();
        return fullName.contains(normalizedTrainerName)
                || training.getTrainer().getUsername().toLowerCase().contains(normalizedTrainerName);
    }

    private boolean matchesTraineeName(Training training, String normalizedTraineeName) {
        if (normalizedTraineeName == null) {
            return true;
        }
        String fullName = (training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName()).toLowerCase();
        return fullName.contains(normalizedTraineeName)
                || training.getTrainee().getUsername().toLowerCase().contains(normalizedTraineeName);
    }

    private boolean matchesTrainingType(Training training, String normalizedTrainingType) {
        if (normalizedTrainingType == null) {
            return true;
        }
        return training.getTrainingType().getName().toLowerCase().contains(normalizedTrainingType);
    }

    private String normalize(String value) {
        return StringUtils.isBlank(value) ? null : value.trim().toLowerCase();
    }
}
