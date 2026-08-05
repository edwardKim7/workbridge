package com.workbridge.server.dto.approval;

import com.workbridge.server.domain.approval.*;
import java.time.LocalDateTime;

public record ApprovalResponse(Long approvalId, String approvalNo, ApprovalType approvalType, String businessType,
    Long businessId, String businessName, Long requesterId, String requesterName, Long approverId, String approverName,
    String title, ApprovalPayload payload, ApprovalStatus status, LocalDateTime requestedAt, LocalDateTime processedAt,
    LocalDateTime canceledAt, LocalDateTime createdAt, LocalDateTime updatedAt, long version) {
}
