package io.jessytsiriniaina.businessoperationmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelRequestDto(@NotBlank @Size(max = 2000) String reason) {}
