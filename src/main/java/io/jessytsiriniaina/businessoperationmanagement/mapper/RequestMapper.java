package io.jessytsiriniaina.businessoperationmanagement.mapper;

import io.jessytsiriniaina.businessoperationmanagement.dto.request.RequestResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.dto.requestcomment.RequestCommentResponseDto;
import io.jessytsiriniaina.businessoperationmanagement.entity.Request;
import io.jessytsiriniaina.businessoperationmanagement.entity.RequestComment;
import io.jessytsiriniaina.businessoperationmanagement.mapper.RequestCommentMapper;
import java.util.List;

public final class RequestMapper {

    private RequestMapper() {}

    public static RequestResponseDto toResponse(Request request) {
        return toResponse(request, List.of(), 0L);
    }

    public static RequestResponseDto toResponse(Request request, List<RequestComment> comments, long commentCount) {
        List<RequestCommentResponseDto> commentDtos =
                comments.stream().map(RequestCommentMapper::toResponse).toList();
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
                request.getDeletedAt(),
                request.getRejectionReason(),
                request.getCancellationReason(),
                request.getStartedAt(),
                request.getApprovedAt(),
                request.getRejectedAt(),
                request.getCancelledAt(),
                commentCount,
                commentDtos);
    }
}
