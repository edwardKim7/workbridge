package com.workbridge.server.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpportunityStageTest {

    @Test
    void stageDerivesStatusAndEnforcesProbabilityBoundary() {
        assertThat(OpportunityStage.PROPOSAL.toStatus()).isEqualTo(OpportunityStatus.OPEN);
        assertThat(OpportunityStage.WON.toStatus()).isEqualTo(OpportunityStatus.CLOSED);
        assertThat(OpportunityStage.LOST.toStatus()).isEqualTo(OpportunityStatus.CLOSED);

        assertThat(OpportunityStage.PROPOSAL.acceptsProbability(0)).isTrue();
        assertThat(OpportunityStage.PROPOSAL.acceptsProbability(99)).isTrue();
        assertThat(OpportunityStage.PROPOSAL.acceptsProbability(100)).isFalse();
        assertThat(OpportunityStage.WON.acceptsProbability(100)).isTrue();
        assertThat(OpportunityStage.WON.acceptsProbability(99)).isFalse();
        assertThat(OpportunityStage.LOST.acceptsProbability(0)).isTrue();
        assertThat(OpportunityStage.LOST.acceptsProbability(1)).isFalse();
    }

    @Test
    void closedStageCanRemainSameButCannotMoveToAnotherStage() {
        assertThat(OpportunityStage.WON.canTransitionFrom(OpportunityStage.WON)).isTrue();
        assertThat(OpportunityStage.LOST.canTransitionFrom(OpportunityStage.LOST)).isTrue();
        assertThat(OpportunityStage.NEGOTIATION.canTransitionFrom(OpportunityStage.WON)).isFalse();
        assertThat(OpportunityStage.WON.canTransitionFrom(OpportunityStage.NEGOTIATION)).isTrue();
    }
}
