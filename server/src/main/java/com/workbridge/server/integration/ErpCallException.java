package com.workbridge.server.integration;

public class ErpCallException extends RuntimeException {
    private final boolean temporary;
    private final boolean retryable;
    private final Integer statusCode;
    private final String responseBody;
    private final String code;

    public ErpCallException(String code, String message, boolean temporary, boolean retryable, Integer statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.temporary = temporary;
        this.retryable = retryable;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getCode() {
        return code;
    }
}
