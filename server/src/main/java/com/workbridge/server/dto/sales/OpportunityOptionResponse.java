package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;

public class OpportunityOptionResponse {

    private final Long opportunityId;
    private final String opportunityNo;
    private final Long customerId;
    private final String opportunityName;
    private final OpportunityStage stage;
    private final OpportunityStatus status;

    public OpportunityOptionResponse(SalesOpportunityRecord opportunity) {
        this.opportunityId = opportunity.getOpportunityId();
        this.opportunityNo = opportunity.getOpportunityNo();
        this.customerId = opportunity.getCustomerId();
        this.opportunityName = opportunity.getOpportunityName();
        this.stage = opportunity.getStage();
        this.status = opportunity.getStatus();
    }

    public Long getOpportunityId() { return opportunityId; }
    public String getOpportunityNo() { return opportunityNo; }
    public Long getCustomerId() { return customerId; }
    public String getOpportunityName() { return opportunityName; }
    public OpportunityStage getStage() { return stage; }
    public OpportunityStatus getStatus() { return status; }
}
