package com.login.gymcrm.service.validator;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.service.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EntityValidator {

    public void requireId(String id, String message) {
        if (StringUtils.isBlank(id)) {
            throw new ValidationException(message);
        }
    }

    public void requireNames(String firstName, String lastName) {
        if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
            throw new ValidationException("First and last name are required");
        }
    }

    public void requireValue(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ValidationException(message);
        }
    }

    public void requireDate(LocalDate date, String message) {
        if (date == null) {
            throw new ValidationException(message);
        }
    }

    public void requirePositive(int value, String message) {
        if (value <= 0) {
            throw new ValidationException(message);
        }
    }

    public void validateDateRange(LocalDate fromDate, LocalDate toDate, String message) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException(message);
        }
    }

    public void validateTraineeForUpdate(Trainee trainee) {
        if (trainee == null || StringUtils.isBlank(trainee.getId())) {
            throw new ValidationException("Trainee id is required for update");
        }
        requireNames(trainee.getFirstName(), trainee.getLastName());
    }

    public void validateTrainerForUpdate(Trainer trainer) {
        if (trainer == null || StringUtils.isBlank(trainer.getId())) {
            throw new ValidationException("Trainer id is required for update");
        }
        requireNames(trainer.getFirstName(), trainer.getLastName());
        requireValue(trainer.getSpecialization(), "Specialization is required");
    }

    public void validateTrainerIds(List<String> trainerIds) {
        if (trainerIds == null || trainerIds.isEmpty()) {
            throw new ValidationException("At least one trainer id is required");
        }
        boolean hasBlank = trainerIds.stream().anyMatch(StringUtils::isBlank);
        if (hasBlank) {
            throw new ValidationException("Trainer ids must be non-empty");
        }
    }
}
