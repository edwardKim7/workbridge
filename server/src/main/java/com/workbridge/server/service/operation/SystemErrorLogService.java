package com.workbridge.server.service.operation;

import com.workbridge.server.audit.TraceIdContext;
import com.workbridge.server.domain.operation.SystemErrorRecord;
import com.workbridge.server.security.WorkBridgePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class SystemErrorLogService {

    private static final Logger log = LoggerFactory.getLogger(SystemErrorLogService.class);

    private final SystemErrorTransactionWriter writer;
    private final Clock clock;

    public SystemErrorLogService(SystemErrorTransactionWriter writer, Clock clock) {
        this.writer = writer;
        this.clock = clock;
    }

    public void safeRecord(
            HttpServletRequest request,
            int status,
            String code,
            String message,
            Throwable exception
    ) {
        try {
            writer.write(toRecord(request, status, code, message, exception));
        } catch (RuntimeException writeFailure) {
            log.warn(
                    "System error metadata could not be stored. traceId={}, cause={}",
                    TraceIdContext.currentTraceId(),
                    writeFailure.getClass().getSimpleName()
            );
        }
    }

    private SystemErrorRecord toRecord(
            HttpServletRequest request,
            int status,
            String code,
            String message,
            Throwable exception
    ) {
        SystemErrorRecord record = new SystemErrorRecord();
        record.setTraceId(safe(TraceIdContext.currentTraceId(), 64, "NO_TRACE_ID"));
        record.setEmployeeId(currentEmployeeId());
        record.setRequestMethod(safe(request.getMethod(), 10, "UNKNOWN"));
        record.setRequestPath(safe(request.getRequestURI(), 500, "/unknown"));
        record.setHttpStatus(status);
        record.setErrorCode(safe(code, 100, "UNKNOWN"));
        record.setExceptionType(exception == null
                ? null
                : safe(exception.getClass().getName(), 300, null));
        record.setSafeMessage(safe(message, 1000, "요청 처리 오류"));
        record.setLogReference("traceId=" + record.getTraceId());
        record.setOccurredAt(LocalDateTime.now(clock));
        return record;
    }

    private Long currentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WorkBridgePrincipal principal) {
            return principal.getEmployeeId();
        }
        return null;
    }

    private String safe(String value, int maxLength, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
