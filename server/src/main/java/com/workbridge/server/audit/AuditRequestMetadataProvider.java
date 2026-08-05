package com.workbridge.server.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditRequestMetadataProvider {

    public AuditRequestMetadata current() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes attributes)) {
            return new AuditRequestMetadata(null, null, safeTraceId());
        }

        HttpServletRequest request = attributes.getRequest();
        return new AuditRequestMetadata(
                truncate(request.getRemoteAddr(), 64),
                truncate(request.getHeader("User-Agent"), 500),
                safeTraceId()
        );
    }

    private String safeTraceId() {
        String traceId = TraceIdContext.currentTraceId();
        return traceId == null || traceId.isBlank() ? "NO_TRACE_ID" : truncate(traceId, 64);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
