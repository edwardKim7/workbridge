package com.workbridge.server.dto.approval;

import com.workbridge.server.domain.approval.*;
import java.time.LocalDateTime;

public record ApprovalHistoryResponse(Long approvalHistoryId, ApprovalStatus fromStatus, ApprovalStatus toStatus,
    ApprovalAction action, Long actorId, String actorName, String opinion, String traceId, LocalDateTime actedAt) {
    public ApprovalHistoryResponse(ApprovalHistoryRecord r) {
        this(r.getApprovalHistoryId(), r.getFromStatus(), r.getToStatus(), r.getAction(), r.getActorId(),
            r.getActorName(), r.getOpinion(), r.getTraceId(), r.getActedAt());
    }
}
