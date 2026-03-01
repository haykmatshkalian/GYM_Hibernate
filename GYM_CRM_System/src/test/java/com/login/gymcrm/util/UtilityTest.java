package com.login.gymcrm.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilityTest {

    @Test
    void passwordGeneratorRejectsInvalidLength() {
        RandomPasswordGenerator generator = new RandomPasswordGenerator();
        assertThatThrownBy(() -> generator.generate(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void passwordGeneratorGeneratesRequestedLength() {
        RandomPasswordGenerator generator = new RandomPasswordGenerator();
        assertThat(generator.generate(12)).hasSize(12);
    }
}
