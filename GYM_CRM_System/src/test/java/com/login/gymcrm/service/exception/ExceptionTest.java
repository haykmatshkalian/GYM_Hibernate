package com.login.gymcrm.service.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void exceptionsCarryMessage() {
        EntityNotFoundException notFound = new EntityNotFoundException("missing");
        ValidationException validation = new ValidationException("bad");

        assertThat(notFound.getMessage()).isEqualTo("missing");
        assertThat(validation.getMessage()).isEqualTo("bad");
    }
}
