package com.workbridge.server.domain.sales;

import java.math.BigDecimal;

public class SalesDashboardSummary {

    private long totalCustomers;
    private long activeCustomers;
    private long prospectCustomers;
    private long openOpportunities;
    private long wonOpportunities;
    private long lostOpportunities;
    private long closingThisMonth;
    private BigDecimal openPipelineAmount;
    private BigDecimal weightedPipelineAmount;

    public SalesDashboardSummary() {
    }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getActiveCustomers() { return activeCustomers; }
    public void setActiveCustomers(long activeCustomers) { this.activeCustomers = activeCustomers; }
    public long getProspectCustomers() { return prospectCustomers; }
    public void setProspectCustomers(long prospectCustomers) { this.prospectCustomers = prospectCustomers; }
    public long getOpenOpportunities() { return openOpportunities; }
    public void setOpenOpportunities(long openOpportunities) { this.openOpportunities = openOpportunities; }
    public long getWonOpportunities() { return wonOpportunities; }
    public void setWonOpportunities(long wonOpportunities) { this.wonOpportunities = wonOpportunities; }
    public long getLostOpportunities() { return lostOpportunities; }
    public void setLostOpportunities(long lostOpportunities) { this.lostOpportunities = lostOpportunities; }
    public long getClosingThisMonth() { return closingThisMonth; }
    public void setClosingThisMonth(long closingThisMonth) { this.closingThisMonth = closingThisMonth; }
    public BigDecimal getOpenPipelineAmount() { return openPipelineAmount; }
    public void setOpenPipelineAmount(BigDecimal openPipelineAmount) { this.openPipelineAmount = openPipelineAmount; }
    public BigDecimal getWeightedPipelineAmount() { return weightedPipelineAmount; }
    public void setWeightedPipelineAmount(BigDecimal weightedPipelineAmount) { this.weightedPipelineAmount = weightedPipelineAmount; }
}
