package com.workbridge.server.dto.auth;

import java.time.Instant;
import java.util.List;

public record JwtClaims(Long employeeId, String loginId, List<String> roles, String tokenType, Instant issuedAt,
    Instant expiresAt) {
}
