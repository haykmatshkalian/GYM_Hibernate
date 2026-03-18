package com.login.gymcrm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.login.gymcrm.dto.ApiErrorResponse;
import com.login.gymcrm.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class BasicAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public BasicAuthenticationFilter(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if ("POST".equalsIgnoreCase(method) && "/api/trainees".equals(path)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method) && "/api/trainers".equals(path)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            return true;
        }

        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            writeUnauthorized(response, request.getRequestURI(), "Missing or invalid Authorization header");
            return;
        }

        String[] credentials = decodeCredentials(authHeader.substring(BASIC_PREFIX.length()));
        if (credentials == null) {
            writeUnauthorized(response, request.getRequestURI(), "Invalid basic authentication token");
            return;
        }

        try {
            userService.validateCredentials(credentials[0], credentials[1]);
            request.setAttribute("authenticatedUsername", credentials[0]);
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            writeUnauthorized(response, request.getRequestURI(), ex.getMessage());
        }
    }

    private String[] decodeCredentials(String token) {
        try {
            byte[] decoded = Base64.getDecoder().decode(token);
            String pair = new String(decoded, StandardCharsets.UTF_8);
            int delimiterIndex = pair.indexOf(':');
            if (delimiterIndex <= 0 || delimiterIndex == pair.length() - 1) {
                return null;
            }
            return new String[]{pair.substring(0, delimiterIndex), pair.substring(delimiterIndex + 1)};
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String path, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                path
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
