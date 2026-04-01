package com.login.gymcrm.dto;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds
) {
}
