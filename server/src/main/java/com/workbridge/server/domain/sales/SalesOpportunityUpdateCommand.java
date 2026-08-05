package com.workbridge.server.domain.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesOpportunityUpdateCommand {

    private final Long opportunityId;
    private final String opportunityName;
    private final OpportunityStage stage;
    private final BigDecimal expectedRevenue;
    private final LocalDate expectedCloseDate;
    private final int probability;
    private final Long ownerEmployeeId;
    private final OpportunityStatus status;
    private final String description;
    private final Long actorId;
    private final long version;

    public SalesOpportunityUpdateCommand(
            Long opportunityId,
            String opportunityName,
            OpportunityStage stage,
            BigDecimal expectedRevenue,
            LocalDate expectedCloseDate,
            int probability,
            Long ownerEmployeeId,
            OpportunityStatus status,
            String description,
            Long actorId,
            long version
    ) {
        this.opportunityId = opportunityId;
        this.opportunityName = opportunityName;
        this.stage = stage;
        this.expectedRevenue = expectedRevenue;
        this.expectedCloseDate = expectedCloseDate;
        this.probability = probability;
        this.ownerEmployeeId = ownerEmployeeId;
        this.status = status;
        this.description = description;
        this.actorId = actorId;
        this.version = version;
    }

    public Long getOpportunityId() { return opportunityId; }
    public String getOpportunityName() { return opportunityName; }
    public OpportunityStage getStage() { return stage; }
    public BigDecimal getExpectedRevenue() { return expectedRevenue; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public int getProbability() { return probability; }
    public Long getOwnerEmployeeId() { return ownerEmployeeId; }
    public OpportunityStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public Long getActorId() { return actorId; }
    public long getVersion() { return version; }
}
