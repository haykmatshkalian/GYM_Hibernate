package com.login.gymcrm.service.validator;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.service.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityValidatorTest {

    private final EntityValidator validator = new EntityValidator();

    @Test
    void requireIdRejectsBlankAndAcceptsValid() {
        assertThatThrownBy(() -> validator.requireId(" ", "id required"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("id required");

        assertThatCode(() -> validator.requireId("abc", "id required"))
                .doesNotThrowAnyException();
    }

    @Test
    void basicPrimitiveValidationsWork() {
        assertThatThrownBy(() -> validator.requireNames("", "Smith"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> validator.requireValue(" ", "value required"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("value required");
        assertThatThrownBy(() -> validator.requireDate(null, "date required"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("date required");
        assertThatThrownBy(() -> validator.requirePositive(0, "positive required"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("positive required");

        assertThatCode(() -> validator.requireNames("John", "Smith")).doesNotThrowAnyException();
        assertThatCode(() -> validator.requireValue("Strength", "value required")).doesNotThrowAnyException();
        assertThatCode(() -> validator.requireDate(LocalDate.now(), "date required")).doesNotThrowAnyException();
        assertThatCode(() -> validator.requirePositive(1, "positive required")).doesNotThrowAnyException();
    }

    @Test
    void dateRangeValidationWorks() {
        assertThatThrownBy(() -> validator.validateDateRange(
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2026, 5, 1),
                "invalid range"
        )).isInstanceOf(ValidationException.class)
                .hasMessage("invalid range");

        assertThatCode(() -> validator.validateDateRange(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 2),
                "invalid range"
        )).doesNotThrowAnyException();
    }

    @Test
    void validateTraineeForUpdateValidatesRequiredFields() {
        assertThatThrownBy(() -> validator.validateTraineeForUpdate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainee id is required");

        Trainee missingId = new Trainee();
        missingId.setUser(new User(UUID.randomUUID().toString(), "A", "B", "a.b", "pwd", true));
        assertThatThrownBy(() -> validator.validateTraineeForUpdate(missingId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainee id is required");

        Trainee valid = new Trainee();
        valid.setId(UUID.randomUUID().toString());
        valid.setUser(new User(UUID.randomUUID().toString(), "Alex", "Stone", "alex.stone", "pwd", true));

        assertThatCode(() -> validator.validateTraineeForUpdate(valid)).doesNotThrowAnyException();
    }

    @Test
    void validateTrainerForUpdateAndTrainerIdsWork() {
        assertThatThrownBy(() -> validator.validateTrainerForUpdate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainer id is required");

        Trainer missingSpecialization = new Trainer();
        missingSpecialization.setId(UUID.randomUUID().toString());
        missingSpecialization.setUser(new User(UUID.randomUUID().toString(), "Sarah", "Cole", "sarah.cole", "pwd", true));
        missingSpecialization.setSpecialization(" ");

        assertThatThrownBy(() -> validator.validateTrainerForUpdate(missingSpecialization))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Specialization is required");

        Trainer valid = new Trainer();
        valid.setId(UUID.randomUUID().toString());
        valid.setUser(new User(UUID.randomUUID().toString(), "Leo", "King", "leo.king", "pwd", true));
        valid.setSpecialization("Cardio");

        assertThatCode(() -> validator.validateTrainerForUpdate(valid)).doesNotThrowAnyException();

        String trainerId1 = UUID.randomUUID().toString();
        String trainerId2 = UUID.randomUUID().toString();

        assertThatThrownBy(() -> validator.validateTrainerIds(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one trainer id");
        assertThatThrownBy(() -> validator.validateTrainerIds(List.of(trainerId1, " ")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("non-empty");

        assertThatCode(() -> validator.validateTrainerIds(List.of(trainerId1, trainerId2))).doesNotThrowAnyException();
    }
}
