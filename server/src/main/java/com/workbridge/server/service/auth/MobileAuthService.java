package com.workbridge.server.service.auth;

import com.workbridge.server.audit.AuditRequestMetadata;
import com.workbridge.server.audit.AuditRequestMetadataProvider;
import com.workbridge.server.config.JwtProperties;
import com.workbridge.server.domain.auth.RefreshTokenRecord;
import com.workbridge.server.dto.auth.*;
import com.workbridge.server.exception.*;
import com.workbridge.server.repository.auth.RefreshTokenRepository;
import com.workbridge.server.security.*;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class MobileAuthService {
    private final EmployeeAuthenticationService authenticationService;
    private final PrincipalLoader principalLoader;
    private final JwtTokenService jwt;
    private final JwtProperties properties;
    private final RefreshTokenRepository tokens;
    private final CurrentEmployee currentEmployee;
    private final AuditRequestMetadataProvider metadata;
    private final AuditService audit;
    private final Clock clock;
    public MobileAuthService(EmployeeAuthenticationService authenticationService, PrincipalLoader principalLoader,
        JwtTokenService jwt, JwtProperties properties, RefreshTokenRepository tokens, CurrentEmployee currentEmployee,
        AuditRequestMetadataProvider metadata, AuditService audit, Clock clock) {
        this.authenticationService = authenticationService;
        this.principalLoader = principalLoader;
        this.jwt = jwt;
        this.properties = properties;
        this.tokens = tokens;
        this.currentEmployee = currentEmployee;
        this.metadata = metadata;
        this.audit = audit;
        this.clock = clock;
    }
    @Transactional
    public MobileAuthResponse login(MobileLoginRequest request) {
        WorkBridgePrincipal p;
        try {
            p = authenticationService.authenticate(request.loginId(), request.password());
        } catch (AuthenticationException e) {
            audit.safeRecordIndependent(null, "MOBILE_LOGIN_FAILURE", "EMPLOYEE", null, null, Map.of("loginId",
                mask(request.loginId())));
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }
        requireMobileRole(p);
        MobileAuthResponse result = issuePair(p, request.deviceInfo());
        audit.safeRecordIndependent(p.getEmployeeId(), "MOBILE_LOGIN_SUCCESS", "EMPLOYEE", String.valueOf(p
            .getEmployeeId()), null, Map.of("roles", p.getRoleCodes()));
        return result;
    }
    @Transactional
    public MobileAuthResponse refresh(TokenRefreshRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        String hash = jwt.hashRefreshToken(request.refreshToken());
        RefreshTokenRecord previous = tokens.findActiveForUpdate(hash, now).orElseThrow(() -> new BusinessException(ErrorCode
            .REFRESH_TOKEN_REUSED));
        WorkBridgePrincipal p = principalLoader.load(previous.getEmployeeId());
        requireMobileRole(p);
        IssuedPair pair = createPair(p, request.deviceInfo(), now);
        if (!tokens.rotate(previous.getRefreshTokenId(), pair.record().getRefreshTokenId(), now)) throw new BusinessException(ErrorCode
            .REFRESH_TOKEN_REUSED);
        return pair.response();
    }
    @Transactional
    public void logout(MobileLogoutRequest request) {
        Long employeeId = currentEmployee.requireEmployeeId();
        String hash = jwt.hashRefreshToken(request.refreshToken());
        tokens.revoke(hash, employeeId, LocalDateTime.now(clock));
        audit.safeRecordIndependent(employeeId, "MOBILE_LOGOUT", "EMPLOYEE", String.valueOf(employeeId),
            null, null);
    }
    private MobileAuthResponse issuePair(WorkBridgePrincipal p, String device) {
        return createPair(p, device, LocalDateTime.now(clock)).response();
    }
    private IssuedPair createPair(WorkBridgePrincipal p, String device, LocalDateTime now) {
        JwtTokenService.IssuedAccessToken access = jwt.issue(p);
        String raw = jwt.newRefreshToken();
        RefreshTokenRecord r = new RefreshTokenRecord();
        r.setEmployeeId(p.getEmployeeId());
        r.setTokenHash(jwt.hashRefreshToken(raw));
        r.setIssuedAt(now);
        r.setExpiresAt(now.plus(properties.getRefreshTtl()));
        AuditRequestMetadata m = metadata.current();
        r.setDeviceInfo(truncate(device, 300));
        r.setClientIp(m.getClientIp());
        tokens.insert(r);
        return new IssuedPair(r, new MobileAuthResponse("Bearer", access.value(), access.expiresAt(),
            raw, r.getExpiresAt().atZone(clock.getZone()).toInstant(), p.getEmployeeId(), p.getEmployeeNo(),
            p.getEmployeeName(), p.getRoleCodes()));
    }
    private void requireMobileRole(WorkBridgePrincipal p) {
        if (!p.hasRole("APPROVER") && !p.hasRole("ADMIN")) throw new BusinessException(ErrorCode.MOBILE_ROLE_REQUIRED);
    }
    private String truncate(String v, int n) {
        return v == null ? null :
        (v.length() <= n ? v : v.substring(0, n));
    }
    private String mask(String s) {
        if (s == null || s.length()<3) return "***";
        return s.substring(0, 2) + "***";
    }
    private record IssuedPair(RefreshTokenRecord record, MobileAuthResponse response) {
    }
}
