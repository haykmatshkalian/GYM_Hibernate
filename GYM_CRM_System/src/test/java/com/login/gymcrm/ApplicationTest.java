package com.login.gymcrm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ApplicationTest {

    @Test
    void mainBootstrapsContext() {
        assertThatCode(() -> Application.main(new String[0]))
                .doesNotThrowAnyException();
    }
}
