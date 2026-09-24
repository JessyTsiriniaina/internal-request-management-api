package io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestCommentDto(
        @NotBlank @Size(max = 2000) String content,
        @NotNull Long authorId) {
    // TODO Phase 4: replace authorId with authenticated user
}
