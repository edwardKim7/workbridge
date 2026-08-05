package com.workbridge.server.domain.approval;

public record ApprovalUpdateCommand(Long approvalId, ApprovalType approvalType, Long businessId, Long approverId,
    String title, String requestPayload, Long actorId, long version) {
}
