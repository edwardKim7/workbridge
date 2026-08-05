package com.workbridge.server.api.common;

import com.workbridge.server.audit.TraceIdContext;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String traceId;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String message, String traceId, LocalDateTime timestamp) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "요청을 정상적으로 처리했습니다.");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, TraceIdContext.currentTraceId(), LocalDateTime.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getTraceId() {
        return traceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
