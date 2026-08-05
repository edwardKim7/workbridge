package com.workbridge.server.dto.operation;

import com.workbridge.server.domain.approval.ApprovalRecord;
import com.workbridge.server.domain.approval.ApprovalStatus;
import java.time.LocalDateTime;

public record ApprovalOperationItem(
        Long approvalId,
        String approvalNo,
        String title,
        ApprovalStatus status,
        String requesterName,
        String approverName,
        LocalDateTime requestedAt,
        LocalDateTime processedAt) {

    public ApprovalOperationItem(ApprovalRecord record) {
        this(
                record.getApprovalId(),
                record.getApprovalNo(),
                record.getTitle(),
                record.getStatus(),
                record.getRequesterName(),
                record.getApproverName(),
                record.getRequestedAt(),
                record.getProcessedAt());
    }
}
