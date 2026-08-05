package com.workbridge.server.api.common;

public class FieldErrorDetail {

    private final String field;
    private final String rejectedValue;
    private final String message;

    public FieldErrorDetail(String field, String rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }

    public String getMessage() {
        return message;
    }
}
