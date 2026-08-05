package com.workbridge.server.domain.integration;

import java.time.LocalDateTime;

public class ExternalInterfaceAttemptRecord {
    private Long attemptId;
    private Long interfaceId;
    private int attemptNo;
    private InterfaceStatus status;
    private Integer responseStatus;
    private String responseBody;
    private String errorCode;
    private String errorMessage;
    private String traceId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long v) {
        attemptId = v;
    }

    public Long getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Long v) {
        interfaceId = v;
    }

    public int getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(int v) {
        attemptNo = v;
    }

    public InterfaceStatus getStatus() {
        return status;
    }

    public void setStatus(InterfaceStatus v) {
        status = v;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer v) {
        responseStatus = v;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String v) {
        responseBody = v;
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String v) {
        traceId = v;
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
}
