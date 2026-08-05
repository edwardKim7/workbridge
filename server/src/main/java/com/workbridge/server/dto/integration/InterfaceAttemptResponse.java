package com.workbridge.server.dto.integration;

import com.workbridge.server.domain.integration.ExternalInterfaceAttemptRecord;
import com.workbridge.server.domain.integration.InterfaceStatus;
import java.time.LocalDateTime;

public record InterfaceAttemptResponse(
        Long attemptId,
        int attemptNo,
        InterfaceStatus status,
        Integer responseStatus,
        String responseBody,
        String errorCode,
        String errorMessage,
        String traceId,
        LocalDateTime startedAt,
        LocalDateTime completedAt) {

    public InterfaceAttemptResponse(ExternalInterfaceAttemptRecord record) {
        this(
                record.getAttemptId(),
                record.getAttemptNo(),
                record.getStatus(),
                record.getResponseStatus(),
                record.getResponseBody(),
                record.getErrorCode(),
                record.getErrorMessage(),
                record.getTraceId(),
                record.getStartedAt(),
                record.getCompletedAt());
    }
}
