package com.workbridge.server.domain.operation;

import java.time.LocalDateTime;

public class SystemErrorRecord {
    private Long systemErrorId;
    private String traceId;
    private Long employeeId;
    private String employeeName;
    private String requestMethod;
    private String requestPath;
    private int httpStatus;
    private String errorCode;
    private String exceptionType;
    private String safeMessage;
    private String logReference;
    private LocalDateTime occurredAt;

    public Long getSystemErrorId() {
        return systemErrorId;
    }

    public void setSystemErrorId(Long v) {
        systemErrorId = v;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String v) {
        traceId = v;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long v) {
        employeeId = v;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String v) {
        employeeName = v;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String v) {
        requestMethod = v;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String v) {
        requestPath = v;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int v) {
        httpStatus = v;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String v) {
        errorCode = v;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String v) {
        exceptionType = v;
    }

    public String getSafeMessage() {
        return safeMessage;
    }

    public void setSafeMessage(String v) {
        safeMessage = v;
    }

    public String getLogReference() {
        return logReference;
    }

    public void setLogReference(String v) {
        logReference = v;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime v) {
        occurredAt = v;
    }
}
