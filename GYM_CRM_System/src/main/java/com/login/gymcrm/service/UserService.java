package com.login.gymcrm.service;

import com.login.gymcrm.dao.UserDao;
import com.login.gymcrm.model.User;
import com.login.gymcrm.security.exception.AuthorizationException;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.exception.ValidationException;
import com.login.gymcrm.service.validator.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final EntityValidator validator;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, EntityValidator validator, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public void validateCredentials(String username, String password) {
        log.debug("Validating credentials for username={}", username);

        validator.requireValue(username, "Username is required");
        validator.requireValue(password, "Password is required");

        User user = userDao.findByUsername(username.trim())
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found username={}", username);
                    return new AuthorizationException("Invalid username or password");
                });

        if (!user.isActive()) {
            log.warn("Authentication failed: user is inactive username={}", username);
            throw new AuthorizationException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: invalid password username={}", username);
            throw new AuthorizationException("Invalid username or password");
        }

        log.info("Authentication succeeded username={}", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.debug("Password change requested username={}", username);

        validator.requireValue(username, "Username is required");
        validator.requireValue(oldPassword, "Old password is required");
        validator.requireValue(newPassword, "New password is required");

        if (oldPassword.equals(newPassword)) {
            throw new ValidationException("New password must differ from old password");
        }

        User user = userDao.findByUsername(username.trim())
                .orElseThrow(() -> {
                    log.warn("Password change failed: user not found username={}", username);
                    return new EntityNotFoundException("User not found by username: " + username);
                });

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed: old password mismatch username={}", username);
            throw new AuthorizationException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userDao.update(user);

        log.info("Password changed successfully username={}", username);
    }
}
