package com.workbridge.server.dto.auth;

import java.time.Instant;
import java.util.List;

public record MobileAuthResponse(String tokenType, String accessToken, Instant accessExpiresAt, String refreshToken,
    Instant refreshExpiresAt, Long employeeId, String employeeNo, String employeeName, List<String> roles) {
}
