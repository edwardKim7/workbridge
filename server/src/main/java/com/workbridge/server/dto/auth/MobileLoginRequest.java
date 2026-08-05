package com.workbridge.server.dto.auth;

import jakarta.validation.constraints.*;

public record MobileLoginRequest(@NotBlank @Size(max = 50) String loginId, @NotBlank @Size(max = 200) String password,
    @Size(max = 300) String deviceInfo) {
}
