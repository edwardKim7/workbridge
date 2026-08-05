package com.workbridge.server.domain.operation;

import java.time.LocalDateTime;

public class AuditLogRecord {
    private Long auditId;
    private Long actorId;
    private String actorName;
    private String action;
    private String targetType;
    private String targetId;
    private String beforeData;
    private String afterData;
    private String clientIp;
    private String userAgent;
    private String traceId;
    private LocalDateTime createdAt;

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long v) {
        auditId = v;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long v) {
        actorId = v;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String v) {
        actorName = v;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String v) {
        action = v;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String v) {
        targetType = v;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String v) {
        targetId = v;
    }

    public String getBeforeData() {
        return beforeData;
    }

    public void setBeforeData(String v) {
        beforeData = v;
    }

    public String getAfterData() {
        return afterData;
    }

    public void setAfterData(String v) {
        afterData = v;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String v) {
        clientIp = v;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String v) {
        userAgent = v;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String v) {
        traceId = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        createdAt = v;
    }
}
