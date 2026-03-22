package com.login.gymcrm.security;

import com.login.gymcrm.dao.UserDao;
import com.login.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private GymUserDetailsService gymUserDetailsService;

    @Test
    void loadUserByUsernameReturnsActiveUserDetails() {
        User user = new User(UUID.randomUUID().toString(), "John", "Smith", "john", "pass", true);
        when(userDao.findByUsername("john")).thenReturn(Optional.of(user));

        UserDetails userDetails = gymUserDetailsService.loadUserByUsername("john");

        assertThat(userDetails.getUsername()).isEqualTo("john");
        assertThat(userDetails.getPassword()).isEqualTo("pass");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        when(userDao.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymUserDetailsService.loadUserByUsername("john"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: john");
    }
}
