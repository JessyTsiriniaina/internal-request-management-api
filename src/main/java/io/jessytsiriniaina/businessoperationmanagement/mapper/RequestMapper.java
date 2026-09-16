package io.jessytsiriniaina.businessoperationmanagement.mapper;

import io.jessytsiriniaina.businessoperationmanagement.dto.request.RequestResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Request;

public final class RequestMapper {

    private RequestMapper() {}

    public static RequestResponseDto toResponse(Request request) {
        return new RequestResponseDto(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority(),
                request.getType() != null
                        ? new RequestResponseDto.TypeSummary(request.getType().getId(), request.getType().getName())
                        : null,
                request.getCreatedBy() != null
                        ? new RequestResponseDto.UserSummary(
                                request.getCreatedBy().getId(),
                                request.getCreatedBy().getName(),
                                request.getCreatedBy().getEmail())
                        : null,
                request.getAssignedTo() != null
                        ? new RequestResponseDto.UserSummary(
                                request.getAssignedTo().getId(),
                                request.getAssignedTo().getName(),
                                request.getAssignedTo().getEmail())
                        : null,
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.isDeleted(),
                request.getDeletedAt());
    }
}
