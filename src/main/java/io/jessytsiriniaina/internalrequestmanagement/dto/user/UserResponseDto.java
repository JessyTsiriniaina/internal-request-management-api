package io.jessytsiriniaina.internalrequestmanagement.dto.user;

import io.jessytsiriniaina.internalrequestmanagement.enums.UserRole;
import java.time.Instant;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String name,
        String email,
        UserRole role,
        DepartmentSummary department,
        Instant createdAt,
        Instant updatedAt) {

    public record DepartmentSummary(Long id, String name) {}
}
