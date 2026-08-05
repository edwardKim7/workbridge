package com.workbridge.server.dto.migration;

import com.workbridge.server.domain.migration.MigrationErrorRecord;

public record MigrationErrorResponse(
        Long migrationErrorId,
        int rowNo,
        String fieldName,
        String errorCode,
        String errorMessage,
        String rawValue) {

    public MigrationErrorResponse(MigrationErrorRecord record) {
        this(
                record.getMigrationErrorId(),
                record.getRowNo(),
                record.getFieldName(),
                record.getErrorCode(),
                record.getErrorMessage(),
                record.getRawValue());
    }
}
