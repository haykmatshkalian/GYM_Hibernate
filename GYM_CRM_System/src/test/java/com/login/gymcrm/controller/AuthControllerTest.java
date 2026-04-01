package com.login.gymcrm.controller;

import com.login.gymcrm.dto.AuthTokenResponse;
import com.login.gymcrm.dto.ChangePasswordRequest;
import com.login.gymcrm.dto.LoginRequest;
import com.login.gymcrm.dto.MessageResponse;
import com.login.gymcrm.service.AuthenticationService;
import com.login.gymcrm.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsOkWhenCredentialsAreValid() {
        AuthTokenResponse tokenResponse = new AuthTokenResponse("Bearer", "token", 3600);
        when(authenticationService.login("john", "pass")).thenReturn(tokenResponse);

        ResponseEntity<AuthTokenResponse> response = authController.login(new LoginRequest("john", "pass"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tokenResponse);
        verify(authenticationService).login("john", "pass");
    }

    @Test
    void loginByQueryParamsReturnsOkWhenCredentialsAreValid() {
        AuthTokenResponse tokenResponse = new AuthTokenResponse("Bearer", "token", 3600);
        when(authenticationService.login("john", "pass")).thenReturn(tokenResponse);

        ResponseEntity<AuthTokenResponse> response = authController.login("john", "pass");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tokenResponse);
        verify(authenticationService).login("john", "pass");
    }

    @Test
    void changeLoginReturnsOkWhenPasswordUpdated() {
        ChangePasswordRequest request = new ChangePasswordRequest("john", "old", "new");

        ResponseEntity<MessageResponse> response = authController.changeLogin(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new MessageResponse("Password changed successfully"));
        verify(userService).changePassword("john", "old", "new");
    }
}
