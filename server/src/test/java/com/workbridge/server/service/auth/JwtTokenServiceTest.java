package com.workbridge.server.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.config.JwtProperties;
import com.workbridge.server.dto.auth.JwtClaims;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.security.WorkBridgePrincipal;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-05T12:00:00Z");

    @Test
    void issuedAccessTokenCanBeVerifiedAndParsed() {
        JwtTokenService service = service(Clock.fixed(NOW, ZoneId.of("Asia/Seoul")), Duration.ofMinutes(15));

        JwtTokenService.IssuedAccessToken issued = service.issue(principal());
        JwtClaims claims = service.parseAccessToken(issued.value());

        assertThat(claims.employeeId()).isEqualTo(3L);
        assertThat(claims.loginId()).isEqualTo("approver");
        assertThat(claims.roles()).containsExactly("APPROVER");
        assertThat(claims.expiresAt()).isEqualTo(NOW.plusSeconds(900));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtTokenService service = service(Clock.fixed(NOW, ZoneId.of("Asia/Seoul")), Duration.ofMinutes(15));
        String token = service.issue(principal()).value();
        String tampered = token.substring(0, token.length() - 2) + "aa";

        assertThatThrownBy(() -> service.parseAccessToken(tampered))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    void expiredTokenIsDistinguishedFromInvalidToken() {
        JwtTokenService issuer = service(Clock.fixed(NOW, ZoneId.of("Asia/Seoul")), Duration.ofSeconds(1));
        String token = issuer.issue(principal()).value();
        JwtTokenService verifier = service(
                Clock.fixed(NOW.plusSeconds(2), ZoneId.of("Asia/Seoul")),
                Duration.ofSeconds(1)
        );

        assertThatThrownBy(() -> verifier.parseAccessToken(token))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED));
    }

    @Test
    void shortSecretIsRejectedAtStartup() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("too-short");
        JwtTokenService service = new JwtTokenService(properties, new ObjectMapper(), Clock.systemUTC());

        assertThatThrownBy(service::initializeSigningKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트");
    }

    private JwtTokenService service(Clock clock, Duration accessTtl) {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("workbridge-test");
        properties.setSecret("0123456789abcdef0123456789abcdef");
        properties.setAccessTtl(accessTtl);
        JwtTokenService service = new JwtTokenService(properties, new ObjectMapper(), clock);
        service.initializeSigningKey();
        return service;
    }

    private WorkBridgePrincipal principal() {
        return new WorkBridgePrincipal(
                3L,
                "WB-2001",
                "approver",
                "박승인",
                "approver@workbridge.example",
                3L,
                "경영지원팀",
                List.of("APPROVER")
        );
    }
}

