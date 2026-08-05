package com.workbridge.server.domain.migration;

public class MigrationErrorRecord {
    private Long migrationErrorId;
    private Long batchId;
    private Long stagingId;
    private int rowNo;
    private String fieldName;
    private String errorCode;
    private String errorMessage;
    private String rawValue;

    public MigrationErrorRecord() {
    }

    public MigrationErrorRecord(Long batchId, int rowNo, String fieldName, String errorCode, String errorMessage, String rawValue) {
        this.batchId = batchId;
        this.rowNo = rowNo;
        this.fieldName = fieldName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.rawValue = rawValue;
    }

    public Long getMigrationErrorId() {
        return migrationErrorId;
    }

    public void setMigrationErrorId(Long v) {
        migrationErrorId = v;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long v) {
        batchId = v;
    }

    public Long getStagingId() {
        return stagingId;
    }

    public void setStagingId(Long v) {
        stagingId = v;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int v) {
        rowNo = v;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String v) {
        fieldName = v;
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

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String v) {
        rawValue = v;
    }
}
