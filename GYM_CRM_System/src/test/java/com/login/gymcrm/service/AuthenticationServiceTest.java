package com.login.gymcrm.service;

import com.login.gymcrm.dto.AuthTokenResponse;
import com.login.gymcrm.security.exception.AuthorizationException;
import com.login.gymcrm.security.jwt.JwtTokenService;
import com.login.gymcrm.security.login.LoginAttemptService;
import com.login.gymcrm.service.validator.EntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private EntityValidator validator;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void loginReturnsJwtTokenOnSuccessfulAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenService.generateToken(eq("john"), any())).thenReturn("jwt-token");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);

        AuthTokenResponse response = authenticationService.login("john", "pass");

        assertThat(response).isEqualTo(new AuthTokenResponse("Bearer", "jwt-token", 3600));
        verify(loginAttemptService).checkBlocked("john");
        verify(loginAttemptService).loginSucceeded("john");
    }

    @Test
    void loginThrowsUnauthorizedWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authenticationService.login("john", "wrong"))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Invalid username or password");

        verify(loginAttemptService).loginFailed("john");
        verify(loginAttemptService, org.mockito.Mockito.times(2)).checkBlocked("john");
    }

    @Test
    void loginThrowsLockedExceptionWhenUserIsBlocked() {
        doThrow(new LockedException("blocked")).when(loginAttemptService).checkBlocked("john");

        assertThatThrownBy(() -> authenticationService.login("john", "wrong"))
                .isInstanceOf(LockedException.class)
                .hasMessage("blocked");
    }
}
