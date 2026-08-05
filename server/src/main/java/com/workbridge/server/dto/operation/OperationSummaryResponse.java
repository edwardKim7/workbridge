package com.workbridge.server.dto.operation;

import com.workbridge.server.dto.integration.InterfaceResponse;
import com.workbridge.server.dto.migration.MigrationBatchResponse;
import com.workbridge.server.dto.system.SystemPingResponse;
import java.util.List;

public record OperationSummaryResponse(
        SystemPingResponse system,
        long retryWaitingCount,
        List<AuditLogResponse> recentLoginFailures,
        List<ApprovalOperationItem> recentApprovals,
        List<InterfaceResponse> recentInterfaces,
        List<MigrationBatchResponse> recentMigrations,
        List<SystemErrorResponse> recentApiErrors,
        List<AuditLogResponse> recentAuditLogs) {}
