package com.workbridge.server.domain.approval;

public enum ApprovalStatus {
    DRAFT, REQUESTED, APPROVED, REJECTED, CANCELED;
    public boolean canTransitionTo(ApprovalStatus target) {
        return(this == DRAFT && (target == REQUESTED || target == CANCELED)) || (this == REQUESTED && (target == APPROVED || target == REJECTED));
    }
    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == CANCELED;
    }
}
