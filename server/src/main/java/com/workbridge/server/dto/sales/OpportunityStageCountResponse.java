package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStageCount;

public class OpportunityStageCountResponse {

    private final OpportunityStage stage;
    private final long count;

    public OpportunityStageCountResponse(OpportunityStageCount value) {
        this.stage = value.getStage();
        this.count = value.getCount();
    }

    public OpportunityStage getStage() { return stage; }
    public long getCount() { return count; }
}
