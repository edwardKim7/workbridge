package com.workbridge.server.domain.integration;

import java.time.LocalDateTime;

public class ExternalInterfaceRecord {
    private Long interfaceId;
    private String interfaceType;
    private String businessType;
    private Long businessId;
    private long businessVersion;
    private String idempotencyKey;
    private String traceId;
    private InterfaceStatus status;
    private String requestMethod;
    private String requestUrl;
    private String requestHeaders;
    private String requestBody;
    private boolean requestTruncated;
    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;
    private boolean responseTruncated;
    private String errorCode;
    private String errorMessage;
    private int retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public Long getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Long v) {
        interfaceId = v;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String v) {
        interfaceType = v;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String v) {
        businessType = v;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long v) {
        businessId = v;
    }

    public long getBusinessVersion() {
        return businessVersion;
    }

    public void setBusinessVersion(long v) {
        businessVersion = v;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String v) {
        idempotencyKey = v;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String v) {
        traceId = v;
    }

    public InterfaceStatus getStatus() {
        return status;
    }

    public void setStatus(InterfaceStatus v) {
        status = v;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String v) {
        requestMethod = v;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String v) {
        requestUrl = v;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String v) {
        requestHeaders = v;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String v) {
        requestBody = v;
    }

    public boolean isRequestTruncated() {
        return requestTruncated;
    }

    public void setRequestTruncated(boolean v) {
        requestTruncated = v;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer v) {
        responseStatus = v;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String v) {
        responseHeaders = v;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String v) {
        responseBody = v;
    }

    public boolean isResponseTruncated() {
        return responseTruncated;
    }

    public void setResponseTruncated(boolean v) {
        responseTruncated = v;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String v) {
        errorCode = v;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String v) {
        errorMessage = v;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int v) {
        retryCount = v;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(LocalDateTime v) {
        nextRetryAt = v;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime v) {
        startedAt = v;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime v) {
        completedAt = v;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long v) {
        createdBy = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        createdAt = v;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime v) {
        updatedAt = v;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long v) {
        version = v;
    }
}
