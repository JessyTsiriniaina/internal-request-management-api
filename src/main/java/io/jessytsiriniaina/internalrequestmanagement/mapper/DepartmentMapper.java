package io.jessytsiriniaina.internalrequestmanagement.mapper;

import io.jessytsiriniaina.internalrequestmanagement.dto.department.DepartmentResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.entity.Department;

public final class DepartmentMapper {

    private DepartmentMapper() {}

    public static DepartmentResponseDto toResponse(Department department) {
        int userCount = department.getUsers() != null ? department.getUsers().size() : 0;
        return new DepartmentResponseDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                userCount);
    }
}
