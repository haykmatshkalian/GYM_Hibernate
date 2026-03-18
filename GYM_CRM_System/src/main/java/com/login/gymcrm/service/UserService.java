package com.login.gymcrm.service;

import com.login.gymcrm.dao.UserDao;
import com.login.gymcrm.model.User;
import com.login.gymcrm.security.exception.AuthorizationException;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.exception.ValidationException;
import com.login.gymcrm.service.validator.EntityValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserDao userDao;
    private final EntityValidator validator;

    public UserService(UserDao userDao, EntityValidator validator) {
        this.userDao = userDao;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public void validateCredentials(String username, String password) {
        validator.requireValue(username, "Username is required");
        validator.requireValue(password, "Password is required");

        User user = userDao.findByUsername(username.trim())
                .orElseThrow(() -> new AuthorizationException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new AuthorizationException("Invalid username or password");
        }
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        validator.requireValue(username, "Username is required");
        validator.requireValue(oldPassword, "Old password is required");
        validator.requireValue(newPassword, "New password is required");

        if (oldPassword.equals(newPassword)) {
            throw new ValidationException("New password must differ from old password");
        }

        User user = userDao.findByUsername(username.trim())
                .orElseThrow(() -> new EntityNotFoundException("User not found by username: " + username));

        if (!user.getPassword().equals(oldPassword)) {
            throw new AuthorizationException("Old password does not match");
        }

        user.setPassword(newPassword);
        userDao.update(user);
    }
}
