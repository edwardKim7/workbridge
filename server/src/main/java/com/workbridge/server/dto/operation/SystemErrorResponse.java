package com.workbridge.server.dto.operation;

import com.workbridge.server.domain.operation.SystemErrorRecord;
import java.time.LocalDateTime;

public record SystemErrorResponse(Long systemErrorId, String traceId, Long employeeId, String employeeName,
    String requestMethod, String requestPath, int httpStatus, String errorCode, String exceptionType,
    String safeMessage, LocalDateTime occurredAt) {
    public SystemErrorResponse(SystemErrorRecord r) {
        this(r.getSystemErrorId(), r.getTraceId(), r.getEmployeeId(), r.getEmployeeName(), r.getRequestMethod(),
            r.getRequestPath(), r.getHttpStatus(), r.getErrorCode(), r.getExceptionType(), r.getSafeMessage(),
            r.getOccurredAt());
    }
}
