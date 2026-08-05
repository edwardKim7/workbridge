package com.workbridge.server.domain.sales;

public enum OpportunityStage {
    LEAD(false),
    QUALIFIED(false),
    PROPOSAL(false),
    NEGOTIATION(false),
    WON(true),
    LOST(true);

    private final boolean closed;

    OpportunityStage(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public OpportunityStatus toStatus() {
        return closed ? OpportunityStatus.CLOSED : OpportunityStatus.OPEN;
    }

    public boolean acceptsProbability(int probability) {
        if (this == WON) {
            return probability == 100;
        }
        if (this == LOST) {
            return probability == 0;
        }
        return probability >= 0 && probability <= 99;
    }

    public boolean canTransitionFrom(OpportunityStage currentStage) {
        return currentStage == null || !currentStage.isClosed() || currentStage == this;
    }
}
