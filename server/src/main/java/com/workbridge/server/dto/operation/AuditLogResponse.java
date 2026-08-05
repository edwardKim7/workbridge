package com.workbridge.server.dto.operation;

import com.workbridge.server.domain.operation.AuditLogRecord;
import java.time.LocalDateTime;

public record AuditLogResponse(
        Long auditId,
        Long actorId,
        String actorName,
        String action,
        String targetType,
        String targetId,
        String beforeData,
        String afterData,
        String clientIp,
        String traceId,
        LocalDateTime createdAt) {

    public AuditLogResponse(AuditLogRecord record) {
        this(
                record.getAuditId(),
                record.getActorId(),
                record.getActorName(),
                record.getAction(),
                record.getTargetType(),
                record.getTargetId(),
                record.getBeforeData(),
                record.getAfterData(),
                record.getClientIp(),
                record.getTraceId(),
                record.getCreatedAt());
    }
}
