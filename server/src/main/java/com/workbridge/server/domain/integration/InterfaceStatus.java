package com.workbridge.server.domain.integration;

public enum InterfaceStatus {
    PENDING, PROCESSING, SUCCESS, TEMPORARY_FAILED, PERMANENT_FAILED, RETRYING;
    public boolean canRetry() {
        return this == TEMPORARY_FAILED || this == PERMANENT_FAILED;
    }
}
