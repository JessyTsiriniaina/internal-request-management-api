package io.jessytsiriniaina.internalrequestmanagement.dto.department;

public record DepartmentResponseDto(
        Long id,
        String name,
        String description,
        int userCount) {}
