package com.login.gymcrm.service;

import com.login.gymcrm.dto.AuthTokenResponse;
import com.login.gymcrm.security.exception.AuthorizationException;
import com.login.gymcrm.security.jwt.JwtTokenService;
import com.login.gymcrm.security.login.LoginAttemptService;
import com.login.gymcrm.service.validator.EntityValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthenticationService {

    private static final String BEARER = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final LoginAttemptService loginAttemptService;
    private final EntityValidator validator;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenService jwtTokenService,
                                 LoginAttemptService loginAttemptService,
                                 EntityValidator validator) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.loginAttemptService = loginAttemptService;
        this.validator = validator;
    }

    public AuthTokenResponse login(String username, String password) {
        validator.requireValue(username, "Username is required");
        validator.requireValue(password, "Password is required");

        String normalizedUsername = username.trim();
        loginAttemptService.checkBlocked(normalizedUsername);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedUsername, password)
            );

            loginAttemptService.loginSucceeded(normalizedUsername);

            String token = jwtTokenService.generateToken(authentication.getName(), authentication.getAuthorities());
            return new AuthTokenResponse(BEARER, token, jwtTokenService.getExpirationSeconds());
        } catch (BadCredentialsException | DisabledException ex) {
            loginAttemptService.loginFailed(normalizedUsername);
            loginAttemptService.checkBlocked(normalizedUsername);
            throw new AuthorizationException("Invalid username or password");
        } catch (AuthenticationException ex) {
            throw new AuthorizationException("Authentication failed");
        }
    }
}
