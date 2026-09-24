package io.jessytsiriniaina.internalrequestmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequestDto(@NotBlank @Size(max = 2000) String reason) {}
