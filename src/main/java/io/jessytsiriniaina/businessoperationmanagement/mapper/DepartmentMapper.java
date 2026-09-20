package io.jessytsiriniaina.businessoperationmanagement.mapper;

import io.jessytsiriniaina.businessoperationmanagement.dto.department.DepartmentResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Department;

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
