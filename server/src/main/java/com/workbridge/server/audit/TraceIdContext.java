package com.workbridge.server.audit;

import org.slf4j.MDC;

public final class TraceIdContext {

    public static final String MDC_KEY = "traceId";
    public static final String REQUEST_ATTRIBUTE = "workbridge.traceId";
    public static final String HEADER_NAME = "X-Trace-Id";

    private TraceIdContext() {
    }

    public static String currentTraceId() {
        String traceId = MDC.get(MDC_KEY);
        return traceId == null || traceId.isBlank() ? "unavailable" : traceId;
    }
}
