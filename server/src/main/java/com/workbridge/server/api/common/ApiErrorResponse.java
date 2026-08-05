package com.workbridge.server.api.common;

import com.workbridge.server.audit.TraceIdContext;

import java.time.LocalDateTime;
import java.util.List;

public class ApiErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final List<FieldErrorDetail> fieldErrors;
    private final String traceId;
    private final String path;

    public ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String code,
            String message,
            List<FieldErrorDetail> fieldErrors,
            String traceId,
            String path
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
        this.fieldErrors = List.copyOf(fieldErrors);
        this.traceId = traceId;
        this.path = path;
    }

    public static ApiErrorResponse of(
            int status,
            String code,
            String message,
            List<FieldErrorDetail> fieldErrors,
            String path
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                code,
                message,
                fieldErrors,
                TraceIdContext.currentTraceId(),
                path
        );
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getPath() {
        return path;
    }
}
