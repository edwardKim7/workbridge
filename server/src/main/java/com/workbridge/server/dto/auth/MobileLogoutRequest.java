package com.workbridge.server.dto.auth;

import jakarta.validation.constraints.*;

public record MobileLogoutRequest(@NotBlank @Size(max = 500) String refreshToken) {
}
