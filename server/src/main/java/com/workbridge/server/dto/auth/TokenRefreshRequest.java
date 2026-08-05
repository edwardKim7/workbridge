package com.workbridge.server.dto.auth;

import jakarta.validation.constraints.*;

public record TokenRefreshRequest(@NotBlank @Size(max = 500) String refreshToken, @Size(max = 300) String deviceInfo) {
}
