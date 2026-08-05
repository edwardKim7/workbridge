package com.workbridge.server.dto.integration;

import com.workbridge.server.domain.integration.*;
import java.time.LocalDateTime;

public record InterfaceResponse(Long interfaceId, String interfaceType, String businessType, Long businessId,
    long businessVersion, String idempotencyKey, String traceId, InterfaceStatus status, Integer responseStatus,
    String errorCode, String errorMessage, int retryCount, LocalDateTime startedAt, LocalDateTime completedAt,
    LocalDateTime createdAt, long version) {
    public InterfaceResponse(ExternalInterfaceRecord r) {
        this(r.getInterfaceId(), r.getInterfaceType(), r.getBusinessType(), r.getBusinessId(), r.getBusinessVersion(),
            r.getIdempotencyKey(), r.getTraceId(), r.getStatus(), r.getResponseStatus(), r.getErrorCode(),
            r.getErrorMessage(), r.getRetryCount(), r.getStartedAt(), r.getCompletedAt(), r.getCreatedAt(),
            r.getVersion());
    }
}
