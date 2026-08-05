package com.workbridge.server.dto.migration;

import com.workbridge.server.domain.migration.*;
import java.time.LocalDateTime;

public record MigrationBatchResponse(Long batchId, String batchNo, String migrationType, String originalFileName,
    long fileSize, MigrationBatchStatus status, int totalCount, int successCount, int failureCount, Long executedBy,
    String executorName, LocalDateTime startedAt, LocalDateTime completedAt, long version) {
    public MigrationBatchResponse(MigrationBatchRecord r) {
        this(r.getBatchId(), r.getBatchNo(), r.getMigrationType(), r.getOriginalFileName(), r.getFileSize(),
            r.getStatus(), r.getTotalCount(), r.getSuccessCount(), r.getFailureCount(), r.getExecutedBy(),
            r.getExecutorName(), r.getStartedAt(), r.getCompletedAt(), r.getVersion());
    }
}
