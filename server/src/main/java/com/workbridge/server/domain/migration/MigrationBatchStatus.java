package com.workbridge.server.domain.migration;

public enum MigrationBatchStatus {
    RECEIVED,
    STAGING,
    VALIDATING,
    APPLYING,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED
}
