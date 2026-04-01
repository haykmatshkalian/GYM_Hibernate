package com.login.gymcrm.security.login;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    @Test
    void blocksUserAfterThreeFailedAttempts() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-01T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(5), clock);

        service.loginFailed("john");
        service.loginFailed("john");
        service.loginFailed("john");

        assertThatThrownBy(() -> service.checkBlocked("john"))
                .isInstanceOf(LockedException.class)
                .hasMessageContaining("User is blocked until");
    }

    @Test
    void unblocksUserAfterBlockDurationExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-01T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(5), clock);

        service.loginFailed("john");
        service.loginFailed("john");
        service.loginFailed("john");

        clock.plus(Duration.ofMinutes(6));

        assertThatCode(() -> service.checkBlocked("john")).doesNotThrowAnyException();
    }

    @Test
    void successfulLoginResetsAttempts() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-01T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(5), clock);

        service.loginFailed("john");
        service.loginFailed("john");
        service.loginSucceeded("john");

        assertThatCode(() -> service.checkBlocked("john")).doesNotThrowAnyException();
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        void plus(Duration duration) {
            this.instant = this.instant.plus(duration);
        }
    }
}
