package io.jessytsiriniaina.internalrequestmanagement.dto.requestcomment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestCommentDto(
        @NotBlank @Size(max = 2000) String content,
        Long authorId) {}
