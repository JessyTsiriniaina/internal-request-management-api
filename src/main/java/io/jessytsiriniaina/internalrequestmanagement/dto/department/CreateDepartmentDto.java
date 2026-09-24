package io.jessytsiriniaina.internalrequestmanagement.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDepartmentDto(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description) {}
