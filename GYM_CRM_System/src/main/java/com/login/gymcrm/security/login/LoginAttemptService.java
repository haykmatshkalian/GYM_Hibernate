package com.login.gymcrm.security.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final Map<String, AttemptState> attemptsByUser = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration blockDuration;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(@Value("${security.bruteforce.max-attempts:3}") int maxAttempts,
                               @Value("${security.bruteforce.block-minutes:5}") long blockMinutes) {
        this(maxAttempts, Duration.ofMinutes(blockMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration blockDuration, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.blockDuration = blockDuration;
        this.clock = clock;
    }

    public void checkBlocked(String username) {
        String key = normalize(username);
        AttemptState state = attemptsByUser.get(key);
        if (state == null) {
            return;
        }

        synchronized (state) {
            Instant now = Instant.now(clock);
            if (state.blockedUntil != null && now.isBefore(state.blockedUntil)) {
                String blockedUntil = DateTimeFormatter.ISO_INSTANT.format(state.blockedUntil);
                throw new LockedException("User is blocked until " + blockedUntil + " due to multiple failed login attempts");
            }

            if (state.blockedUntil != null && !now.isBefore(state.blockedUntil)) {
                attemptsByUser.remove(key);
            }
        }
    }

    public void loginFailed(String username) {
        String key = normalize(username);
        AttemptState state = attemptsByUser.computeIfAbsent(key, unused -> new AttemptState());

        synchronized (state) {
            Instant now = Instant.now(clock);
            if (state.blockedUntil != null && now.isBefore(state.blockedUntil)) {
                return;
            }

            if (state.blockedUntil != null && !now.isBefore(state.blockedUntil)) {
                state.blockedUntil = null;
                state.failedAttempts = 0;
            }

            state.failedAttempts++;
            if (state.failedAttempts >= maxAttempts) {
                state.failedAttempts = 0;
                state.blockedUntil = now.plus(blockDuration);
            }
        }
    }

    public void loginSucceeded(String username) {
        attemptsByUser.remove(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private static final class AttemptState {
        private int failedAttempts;
        private Instant blockedUntil;
    }
}
