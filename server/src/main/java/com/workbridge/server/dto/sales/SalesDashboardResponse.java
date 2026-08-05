package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.OpportunityStageCount;
import com.workbridge.server.domain.sales.SalesDashboardSummary;

import java.math.BigDecimal;
import java.util.List;

public class SalesDashboardResponse {

    private final String scope;
    private final long totalCustomers;
    private final long activeCustomers;
    private final long prospectCustomers;
    private final long openOpportunities;
    private final long wonOpportunities;
    private final long lostOpportunities;
    private final long closingThisMonth;
    private final BigDecimal openPipelineAmount;
    private final BigDecimal weightedPipelineAmount;
    private final List<OpportunityStageCountResponse> stageCounts;

    public SalesDashboardResponse(
            String scope,
            SalesDashboardSummary summary,
            List<OpportunityStageCount> stageCounts
    ) {
        this.scope = scope;
        this.totalCustomers = summary.getTotalCustomers();
        this.activeCustomers = summary.getActiveCustomers();
        this.prospectCustomers = summary.getProspectCustomers();
        this.openOpportunities = summary.getOpenOpportunities();
        this.wonOpportunities = summary.getWonOpportunities();
        this.lostOpportunities = summary.getLostOpportunities();
        this.closingThisMonth = summary.getClosingThisMonth();
        this.openPipelineAmount = zeroIfNull(summary.getOpenPipelineAmount());
        this.weightedPipelineAmount = zeroIfNull(summary.getWeightedPipelineAmount());
        this.stageCounts = stageCounts.stream().map(OpportunityStageCountResponse::new).toList();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public String getScope() { return scope; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getActiveCustomers() { return activeCustomers; }
    public long getProspectCustomers() { return prospectCustomers; }
    public long getOpenOpportunities() { return openOpportunities; }
    public long getWonOpportunities() { return wonOpportunities; }
    public long getLostOpportunities() { return lostOpportunities; }
    public long getClosingThisMonth() { return closingThisMonth; }
    public BigDecimal getOpenPipelineAmount() { return openPipelineAmount; }
    public BigDecimal getWeightedPipelineAmount() { return weightedPipelineAmount; }
    public List<OpportunityStageCountResponse> getStageCounts() { return stageCounts; }
}
