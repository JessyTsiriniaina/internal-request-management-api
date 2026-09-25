package io.jessytsiriniaina.internalrequestmanagement.dto.auth;

import io.jessytsiriniaina.internalrequestmanagement.dto.user.UserResponseDto;

public record AuthResponseDto(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponseDto user) {
    public static AuthResponseDto of(String token, long expiresInMs, UserResponseDto user) {
        return new AuthResponseDto(token, "Bearer", expiresInMs, user);
    }
}
