package com.workbridge.server.approval;

import com.workbridge.server.domain.approval.ApprovalStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalStatusTest {

    @Test
    void draftCanOnlyBeRequestedOrCanceled() {
        assertThat(ApprovalStatus.DRAFT.canTransitionTo(ApprovalStatus.REQUESTED)).isTrue();
        assertThat(ApprovalStatus.DRAFT.canTransitionTo(ApprovalStatus.CANCELED)).isTrue();
        assertThat(ApprovalStatus.DRAFT.canTransitionTo(ApprovalStatus.APPROVED)).isFalse();
        assertThat(ApprovalStatus.DRAFT.canTransitionTo(ApprovalStatus.REJECTED)).isFalse();
    }

    @Test
    void requestedCanOnlyBeApprovedOrRejected() {
        assertThat(ApprovalStatus.REQUESTED.canTransitionTo(ApprovalStatus.APPROVED)).isTrue();
        assertThat(ApprovalStatus.REQUESTED.canTransitionTo(ApprovalStatus.REJECTED)).isTrue();
        assertThat(ApprovalStatus.REQUESTED.canTransitionTo(ApprovalStatus.CANCELED)).isFalse();
        assertThat(ApprovalStatus.REQUESTED.canTransitionTo(ApprovalStatus.DRAFT)).isFalse();
    }

    @Test
    void terminalStatusCannotTransitionAgain() {
        assertThat(ApprovalStatus.APPROVED.isTerminal()).isTrue();
        assertThat(ApprovalStatus.REJECTED.isTerminal()).isTrue();
        assertThat(ApprovalStatus.CANCELED.isTerminal()).isTrue();
        assertThat(ApprovalStatus.APPROVED.canTransitionTo(ApprovalStatus.REJECTED)).isFalse();
    }
}
