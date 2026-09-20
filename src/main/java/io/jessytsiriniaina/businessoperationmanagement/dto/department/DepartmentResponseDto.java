package io.jessytsiriniaina.businessoperationmanagement.dto.department;

public record DepartmentResponseDto(
        Long id,
        String name,
        String description,
        int userCount) {}
