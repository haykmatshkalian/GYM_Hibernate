package com.login.gymcrm.service;

import com.login.gymcrm.dao.UserDao;
import com.login.gymcrm.model.User;
import com.login.gymcrm.security.exception.AuthorizationException;
import com.login.gymcrm.service.exception.EntityNotFoundException;
import com.login.gymcrm.service.exception.ValidationException;
import com.login.gymcrm.service.validator.EntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private EntityValidator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void validateCredentialsPassesWhenUsernameAndPasswordMatch() {
        User user = new User(UUID.randomUUID().toString(), "John", "Smith", "john", "encoded", true);
        when(userDao.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);

        assertThatCode(() -> userService.validateCredentials("john", "pass"))
                .doesNotThrowAnyException();

        verify(userDao).findByUsername("john");
    }

    @Test
    void validateCredentialsThrowsWhenPasswordDoesNotMatch() {
        User user = new User(UUID.randomUUID().toString(), "John", "Smith", "john", "encoded", true);
        when(userDao.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.validateCredentials("john", "wrong"))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void validateCredentialsThrowsWhenUserIsInactive() {
        User user = new User(UUID.randomUUID().toString(), "John", "Smith", "john", "encoded", false);
        when(userDao.findByUsername("john")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.validateCredentials("john", "pass"))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void changePasswordUpdatesExistingUser() {
        User user = new User(UUID.randomUUID().toString(), "John", "Smith", "john", "encoded-old", true);
        when(userDao.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");

        userService.changePassword("john", "old", "new");

        assertThat(user.getPassword()).isEqualTo("encoded-new");
        verify(userDao).update(user);
    }

    @Test
    void changePasswordThrowsWhenNewPasswordEqualsOldPassword() {
        assertThatThrownBy(() -> userService.changePassword("john", "same", "same"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("New password must differ from old password");

        verifyNoInteractions(userDao);
    }

    @Test
    void changePasswordThrowsWhenUserNotFound() {
        when(userDao.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("john", "old", "new"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found by username: john");
    }

    @Test
    void changePasswordThrowsWhenOldPasswordDoesNotMatch() {
        User user = new User(UUID.randomUUID().toString(), "John", "Smith", "john", "encoded-old", true);
        when(userDao.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john", "old", "new"))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Old password does not match");
    }
}
