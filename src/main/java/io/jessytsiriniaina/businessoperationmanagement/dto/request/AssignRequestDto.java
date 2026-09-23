package io.jessytsiriniaina.businessoperationmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignRequestDto(@NotNull Long assignedToId) {}
