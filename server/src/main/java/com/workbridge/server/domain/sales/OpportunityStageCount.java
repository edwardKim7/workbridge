package com.workbridge.server.domain.sales;

public class OpportunityStageCount {

    private OpportunityStage stage;
    private long count;

    public OpportunityStageCount() {
    }

    public OpportunityStageCount(OpportunityStage stage, long count) {
        this.stage = stage;
        this.count = count;
    }

    public OpportunityStage getStage() { return stage; }
    public void setStage(OpportunityStage stage) { this.stage = stage; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
