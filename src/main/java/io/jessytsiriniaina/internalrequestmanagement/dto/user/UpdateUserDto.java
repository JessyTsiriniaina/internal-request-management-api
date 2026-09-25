package io.jessytsiriniaina.internalrequestmanagement.dto.user;

import io.jessytsiriniaina.internalrequestmanagement.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Email @Size(max = 150) String email,
        UserRole role,
        Long departmentId) {}
