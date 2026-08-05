package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.BusinessRegistrationNumber;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OpportunityResponse {

    private final Long opportunityId;
    private final String opportunityNo;
    private final Long customerId;
    private final String customerName;
    private final String businessRegistrationNo;
    private final String formattedBusinessRegistrationNo;
    private final Long customerAssignedSalesId;
    private final CustomerStatus customerStatus;
    private final boolean customerActive;
    private final String opportunityName;
    private final OpportunityStage stage;
    private final BigDecimal expectedRevenue;
    private final LocalDate expectedCloseDate;
    private final int probability;
    private final Long ownerEmployeeId;
    private final String ownerEmployeeNo;
    private final String ownerEmployeeName;
    private final OpportunityStatus status;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final long version;

    public OpportunityResponse(SalesOpportunityRecord opportunity) {
        this.opportunityId = opportunity.getOpportunityId();
        this.opportunityNo = opportunity.getOpportunityNo();
        this.customerId = opportunity.getCustomerId();
        this.customerName = opportunity.getCustomerName();
        this.businessRegistrationNo = opportunity.getBusinessRegistrationNo();
        this.formattedBusinessRegistrationNo = BusinessRegistrationNumber.format(opportunity.getBusinessRegistrationNo());
        this.customerAssignedSalesId = opportunity.getCustomerAssignedSalesId();
        this.customerStatus = opportunity.getCustomerStatus();
        this.customerActive = opportunity.isCustomerActive();
        this.opportunityName = opportunity.getOpportunityName();
        this.stage = opportunity.getStage();
        this.expectedRevenue = opportunity.getExpectedRevenue();
        this.expectedCloseDate = opportunity.getExpectedCloseDate();
        this.probability = opportunity.getProbability();
        this.ownerEmployeeId = opportunity.getOwnerEmployeeId();
        this.ownerEmployeeNo = opportunity.getOwnerEmployeeNo();
        this.ownerEmployeeName = opportunity.getOwnerEmployeeName();
        this.status = opportunity.getStatus();
        this.description = opportunity.getDescription();
        this.createdAt = opportunity.getCreatedAt();
        this.updatedAt = opportunity.getUpdatedAt();
        this.version = opportunity.getVersion();
    }

    public Long getOpportunityId() { return opportunityId; }
    public String getOpportunityNo() { return opportunityNo; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getBusinessRegistrationNo() { return businessRegistrationNo; }
    public String getFormattedBusinessRegistrationNo() { return formattedBusinessRegistrationNo; }
    public Long getCustomerAssignedSalesId() { return customerAssignedSalesId; }
    public CustomerStatus getCustomerStatus() { return customerStatus; }
    public boolean isCustomerActive() { return customerActive; }
    public String getOpportunityName() { return opportunityName; }
    public OpportunityStage getStage() { return stage; }
    public BigDecimal getExpectedRevenue() { return expectedRevenue; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public int getProbability() { return probability; }
    public Long getOwnerEmployeeId() { return ownerEmployeeId; }
    public String getOwnerEmployeeNo() { return ownerEmployeeNo; }
    public String getOwnerEmployeeName() { return ownerEmployeeName; }
    public OpportunityStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
