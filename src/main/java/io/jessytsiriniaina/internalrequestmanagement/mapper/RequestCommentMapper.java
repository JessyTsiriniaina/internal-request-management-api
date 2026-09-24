package io.jessytsiriniaina.internalrequestmanagement.mapper;

import io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment.RequestCommentResponseDto;
import io.jessytsiriniaina.internalrequestmanagement.entity.RequestComment;

public final class RequestCommentMapper {

    private RequestCommentMapper() {}

    public static RequestCommentResponseDto toResponse(RequestComment comment) {
        return new RequestCommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getRequest() != null ? comment.getRequest().getId() : null,
                comment.getAuthor() != null
                        ? new RequestCommentResponseDto.AuthorSummary(
                                comment.getAuthor().getId(),
                                comment.getAuthor().getName(),
                                comment.getAuthor().getEmail())
                        : null,
                comment.getCreatedAt(),
                comment.isDeleted(),
                comment.getDeletedAt());
    }
}
