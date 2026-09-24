package io.jessytsiriniaina.businessoperationmanagement.dto.requestcomment;

import java.time.Instant;

public record RequestCommentResponseDto(
        Long id,
        String content,
        Long requestId,
        AuthorSummary author,
        Instant createdAt,
        boolean deleted,
        Instant deletedAt) {

    public record AuthorSummary(Long id, String name, String email) {}
}
