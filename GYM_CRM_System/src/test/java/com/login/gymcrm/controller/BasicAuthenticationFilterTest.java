package com.login.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.login.gymcrm.security.BasicAuthenticationFilter;
import com.login.gymcrm.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BasicAuthenticationFilterTest {

    @Mock
    private UserService userService;

    @Test
    void protectedEndpointWithoutAuthorizationReturns401() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees/john.smith");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing or invalid Authorization header");
    }

    @Test
    void loginEndpointWithoutAuthorizationIsAllowed() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void publicRegistrationEndpointDoesNotRequireAuthorization() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/trainees");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void actuatorEndpointDoesNotRequireAuthorization() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void protectedEndpointWithValidBasicAuthProceeds() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainers/coach.sarah");
        request.addHeader("Authorization", "Basic " + basic("coach.sarah", "pass"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        verify(userService).validateCredentials("coach.sarah", "pass");
    }

    @Test
    void protectedEndpointWithInvalidCredentialsReturns401() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainers/coach.sarah");
        request.addHeader("Authorization", "Basic " + basic("coach.sarah", "wrong"));

        doThrow(new RuntimeException("Invalid username or password"))
                .when(userService).validateCredentials("coach.sarah", "wrong");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid username or password");
    }

    @Test
    void activationEndpointDoesNotRequireAuthorizationEvenWithInvalidBasicHeader() throws Exception {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(userService, objectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest(
                "PATCH",
                "/api/trainees/user/7c6a5c67-55a7-4242-8a94-fd7e54d27cfe/activation"
        );
        request.addHeader("Authorization", "Basic " + basic("wrong", "wrong"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    private String basic(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
