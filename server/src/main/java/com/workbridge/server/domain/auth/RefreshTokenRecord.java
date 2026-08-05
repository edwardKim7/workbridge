package com.workbridge.server.domain.auth;

import java.time.LocalDateTime;

public class RefreshTokenRecord {
    private Long refreshTokenId;
    private Long employeeId;
    private String tokenHash;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private Long replacedByTokenId;
    private String deviceInfo;
    private String clientIp;
    public Long getRefreshTokenId() {
        return refreshTokenId;
    }
    public void setRefreshTokenId(Long v) {
        refreshTokenId = v;
    }
    public Long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(Long v) {
        employeeId = v;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public void setTokenHash(String v) {
        tokenHash = v;
    }
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    public void setIssuedAt(LocalDateTime v) {
        issuedAt = v;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime v) {
        expiresAt = v;
    }
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    public void setRevokedAt(LocalDateTime v) {
        revokedAt = v;
    }
    public Long getReplacedByTokenId() {
        return replacedByTokenId;
    }
    public void setReplacedByTokenId(Long v) {
        replacedByTokenId = v;
    }
    public String getDeviceInfo() {
        return deviceInfo;
    }
    public void setDeviceInfo(String v) {
        deviceInfo = v;
    }
    public String getClientIp() {
        return clientIp;
    }
    public void setClientIp(String v) {
        clientIp = v;
    }
}
