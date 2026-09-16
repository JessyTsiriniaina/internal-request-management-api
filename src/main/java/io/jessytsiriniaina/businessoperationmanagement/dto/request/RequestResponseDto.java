package io.jessytsiriniaina.businessoperationmanagement.dto.request;

import io.jessytsiriniaina.businessoperationmanagement.enums.RequestPriority;
import io.jessytsiriniaina.businessoperationmanagement.enums.RequestStatus;
import java.time.Instant;

public record RequestResponseDto(
        Long id,
        String title,
        String description,
        RequestStatus status,
        RequestPriority priority,
        TypeSummary type,
        UserSummary createdBy,
        UserSummary assignedTo,
        Instant createdAt,
        Instant updatedAt,
        boolean deleted,
        Instant deletedAt) {

    public record TypeSummary(Long id, String name) {}

    public record UserSummary(Long id, String name, String email) {}
}
