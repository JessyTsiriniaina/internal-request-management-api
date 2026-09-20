package io.jessytsiriniaina.businessoperationmanagement.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentDto(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description) {}
