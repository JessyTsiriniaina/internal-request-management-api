package io.jessytsiriniaina.internalrequestmanagement.dto.request;

import io.jessytsiriniaina.internalrequestmanagement.enums.RequestPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequestDto(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull RequestPriority priority,
        @NotNull Long typeId,
        Long assignedToId) {}
