package io.jessytsiriniaina.businessoperationmanagement.dto.request;

import io.jessytsiriniaina.businessoperationmanagement.enums.RequestPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestDto(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull RequestPriority priority,
        @NotNull Long typeId,
        @NotNull Long createdById,
        Long assignedToId) {}
