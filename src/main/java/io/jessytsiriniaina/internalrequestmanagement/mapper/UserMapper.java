package io.jessytsiriniaina.internalrequestmanagement.mapper;

import io.jessytsiriniaina.internalrequestmanagement.dto.user.UserResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponseDto toResponse(User user) {
        UserResponseDto.DepartmentSummary dept = null;
        if (user.getDepartment() != null) {
            dept = new UserResponseDto.DepartmentSummary(user.getDepartment().getId(), user.getDepartment().getName());
        }
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                dept,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
