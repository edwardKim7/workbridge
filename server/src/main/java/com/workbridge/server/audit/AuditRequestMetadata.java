package com.workbridge.server.audit;

public class AuditRequestMetadata {

    private final String clientIp;
    private final String userAgent;
    private final String traceId;

    public AuditRequestMetadata(String clientIp, String userAgent, String traceId) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.traceId = traceId;
    }

    public String getClientIp() { return clientIp; }
    public String getUserAgent() { return userAgent; }
    public String getTraceId() { return traceId; }
}
