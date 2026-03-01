package com.login.gymcrm.service.validator;

import com.login.gymcrm.model.Trainee;
import com.login.gymcrm.model.Trainer;
import com.login.gymcrm.model.User;
import com.login.gymcrm.service.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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
    void validateTraineeForUpdateValidatesRequiredFields() {
        assertThatThrownBy(() -> validator.validateTraineeForUpdate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainee id is required");

        Trainee missingId = new Trainee();
        missingId.setUser(new User("u1", "A", "B", "a.b", "pwd", true));
        assertThatThrownBy(() -> validator.validateTraineeForUpdate(missingId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainee id is required");

        Trainee valid = new Trainee();
        valid.setId("t1");
        valid.setUser(new User("u2", "Alex", "Stone", "alex.stone", "pwd", true));

        assertThatCode(() -> validator.validateTraineeForUpdate(valid)).doesNotThrowAnyException();
    }

    @Test
    void validateTrainerForUpdateAndTrainerIdsWork() {
        assertThatThrownBy(() -> validator.validateTrainerForUpdate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Trainer id is required");

        Trainer missingSpecialization = new Trainer();
        missingSpecialization.setId("r1");
        missingSpecialization.setUser(new User("u3", "Sarah", "Cole", "sarah.cole", "pwd", true));
        missingSpecialization.setSpecialization(" ");

        assertThatThrownBy(() -> validator.validateTrainerForUpdate(missingSpecialization))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Specialization is required");

        Trainer valid = new Trainer();
        valid.setId("r2");
        valid.setUser(new User("u4", "Leo", "King", "leo.king", "pwd", true));
        valid.setSpecialization("Cardio");

        assertThatCode(() -> validator.validateTrainerForUpdate(valid)).doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validateTrainerIds(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one trainer id");
        assertThatThrownBy(() -> validator.validateTrainerIds(List.of("r1", " ")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("non-empty");

        assertThatCode(() -> validator.validateTrainerIds(List.of("r1", "r2"))).doesNotThrowAnyException();
    }
}
