package com.workbridge.server.service.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.config.JwtProperties;
import com.workbridge.server.dto.auth.JwtClaims;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.security.WorkBridgePrincipal;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final Base64.Encoder URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private byte[] signingKey;

    public JwtTokenService(
            JwtProperties properties,
            ObjectMapper objectMapper,
            Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @PostConstruct
    void initializeSigningKey() {
        String secret = properties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET은 UTF-8 기준 32바이트 이상이어야 합니다.");
        }
        signingKey = secret.getBytes(StandardCharsets.UTF_8);
    }

    public IssuedAccessToken issue(WorkBridgePrincipal principal) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.getAccessTtl());

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", properties.getIssuer());
        claims.put("sub", String.valueOf(principal.getEmployeeId()));
        claims.put("loginId", principal.getLoginId());
        claims.put("roles", principal.getRoleCodes());
        claims.put("tokenType", "access");
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = encode(header) + "." + encode(claims);
        String signature = URL_ENCODER.encodeToString(sign(unsignedToken));
        return new IssuedAccessToken(unsignedToken + "." + signature, expiresAt);
    }

    public JwtClaims parseAccessToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw invalidToken();
            }

            Map<String, Object> header = objectMapper.readValue(
                    URL_DECODER.decode(parts[0]),
                    new TypeReference<>() {});
            if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) {
                throw invalidToken();
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsignedToken), URL_DECODER.decode(parts[2]))) {
                throw invalidToken();
            }

            Map<String, Object> claims = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {});
            if (!properties.getIssuer().equals(claims.get("iss"))
                    || !"access".equals(claims.get("tokenType"))) {
                throw invalidToken();
            }

            Instant issuedAt = Instant.ofEpochSecond(number(claims, "iat"));
            Instant expiresAt = Instant.ofEpochSecond(number(claims, "exp"));
            if (!expiresAt.isAfter(clock.instant())) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }

            Long employeeId = Long.valueOf(String.valueOf(claims.get("sub")));
            String loginId = String.valueOf(claims.get("loginId"));
            List<String> roles = ((List<?>) claims.getOrDefault("roles", List.of()))
                    .stream()
                    .map(String::valueOf)
                    .toList();

            return new JwtClaims(
                    employeeId,
                    loginId,
                    roles,
                    "access",
                    issuedAt,
                    expiresAt);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalidToken();
        }
    }

    public String newRefreshToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    public String hashRefreshToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String encode(Object value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private long number(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private BusinessException invalidToken() {
        return new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    public record IssuedAccessToken(String value, Instant expiresAt) {}
}
