package com.workbridge.server.service.approval;

public record ApprovalCompletedEvent(Long approvalId, Long actorId) {
}
