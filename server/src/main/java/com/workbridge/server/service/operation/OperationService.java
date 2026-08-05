package com.workbridge.server.service.operation;

import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.dto.operation.*;
import com.workbridge.server.dto.integration.InterfaceResponse;
import com.workbridge.server.dto.migration.MigrationBatchResponse;
import com.workbridge.server.exception.*;
import com.workbridge.server.repository.approval.ApprovalRepository;
import com.workbridge.server.repository.audit.AuditLogRepository;
import com.workbridge.server.repository.integration.ExternalInterfaceRepository;
import com.workbridge.server.repository.migration.MigrationRepository;
import com.workbridge.server.repository.operation.SystemErrorRepository;
import com.workbridge.server.service.system.SystemStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationService {
    private final SystemStatusService system;
    private final AuditLogRepository audit;
    private final ApprovalRepository approvals;
    private final ExternalInterfaceRepository interfaces;
    private final MigrationRepository migrations;
    private final SystemErrorRepository errors;
    public OperationService(SystemStatusService s, AuditLogRepository a, ApprovalRepository p, ExternalInterfaceRepository i,
        MigrationRepository m, SystemErrorRepository e) {
        system = s;
        audit = a;
        approvals = p;
        interfaces = i;
        migrations = m;
        errors = e;
    }
    @Transactional(readOnly = true)
    public OperationSummaryResponse summary() {
        return new OperationSummaryResponse(system.getPingStatus(), interfaces.retryWaiting(), audit
            .loginFailures(5).stream().map(AuditLogResponse::new).toList(), approvals.findRecent(5).stream()
            .map(ApprovalOperationItem::new).toList(), interfaces.recent(5).stream().map(InterfaceResponse::new)
            .toList(), migrations.recent(5).stream().map(MigrationBatchResponse::new).toList(), errors
            .recent(5).stream().map(SystemErrorResponse::new).toList(), audit.recent(5).stream().map(AuditLogResponse::new)
            .toList());
    }
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> audits(String action, String trace, int page, int size) {
        check(page, size);
        return PageResponse.of(audit.page(norm(action), norm(trace), page * size, size).stream().map(AuditLogResponse::new)
            .toList(), page, size, audit.count(norm(action), norm(trace)));
    }
    @Transactional(readOnly = true)
    public PageResponse<SystemErrorResponse> errors(String trace, Integer status, int page, int size) {
        check(page, size);
        return PageResponse.of(errors.page(norm(trace), status, page * size, size).stream().map(SystemErrorResponse::new)
            .toList(), page, size, errors.count(norm(trace), status));
    }
    private void check(int p, int s) {
        if (p<0 || s<1 || s> 100) throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    private String norm(String v) {
        return v == null || v.isBlank() ? null :
        v.trim();
    }
}
