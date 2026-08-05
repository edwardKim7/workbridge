package com.workbridge.server.domain.migration;

import java.time.LocalDateTime;

public class MigrationBatchRecord {
    private Long batchId;
    private String batchNo;
    private String migrationType;
    private String originalFileName;
    private long fileSize;
    private String fileHash;
    private String dedupKey;
    private MigrationBatchStatus status;
    private int totalCount;
    private int successCount;
    private int failureCount;
    private Long executedBy;
    private String executorName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorFilePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long v) {
        batchId = v;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String v) {
        batchNo = v;
    }

    public String getMigrationType() {
        return migrationType;
    }

    public void setMigrationType(String v) {
        migrationType = v;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String v) {
        originalFileName = v;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long v) {
        fileSize = v;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String v) {
        fileHash = v;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String v) {
        dedupKey = v;
    }

    public MigrationBatchStatus getStatus() {
        return status;
    }

    public void setStatus(MigrationBatchStatus v) {
        status = v;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int v) {
        totalCount = v;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int v) {
        successCount = v;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int v) {
        failureCount = v;
    }

    public Long getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(Long v) {
        executedBy = v;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String v) {
        executorName = v;
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

    public String getErrorFilePath() {
        return errorFilePath;
    }

    public void setErrorFilePath(String v) {
        errorFilePath = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        createdAt = v;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime v) {
        updatedAt = v;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long v) {
        version = v;
    }
}
