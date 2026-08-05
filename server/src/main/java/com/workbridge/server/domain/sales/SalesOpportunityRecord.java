package com.workbridge.server.domain.sales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SalesOpportunityRecord {

    private Long opportunityId;
    private String opportunityNo;
    private Long customerId;
    private String customerName;
    private String businessRegistrationNo;
    private Long customerAssignedSalesId;
    private CustomerStatus customerStatus;
    private boolean customerActive;
    private String opportunityName;
    private OpportunityStage stage;
    private BigDecimal expectedRevenue;
    private LocalDate expectedCloseDate;
    private int probability;
    private Long ownerEmployeeId;
    private String ownerEmployeeNo;
    private String ownerEmployeeName;
    private OpportunityStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public SalesOpportunityRecord() {
    }

    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityNo() { return opportunityNo; }
    public void setOpportunityNo(String opportunityNo) { this.opportunityNo = opportunityNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getBusinessRegistrationNo() { return businessRegistrationNo; }
    public void setBusinessRegistrationNo(String businessRegistrationNo) { this.businessRegistrationNo = businessRegistrationNo; }
    public Long getCustomerAssignedSalesId() { return customerAssignedSalesId; }
    public void setCustomerAssignedSalesId(Long customerAssignedSalesId) { this.customerAssignedSalesId = customerAssignedSalesId; }
    public CustomerStatus getCustomerStatus() { return customerStatus; }
    public void setCustomerStatus(CustomerStatus customerStatus) { this.customerStatus = customerStatus; }
    public boolean isCustomerActive() { return customerActive; }
    public void setCustomerActive(boolean customerActive) { this.customerActive = customerActive; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public OpportunityStage getStage() { return stage; }
    public void setStage(OpportunityStage stage) { this.stage = stage; }
    public BigDecimal getExpectedRevenue() { return expectedRevenue; }
    public void setExpectedRevenue(BigDecimal expectedRevenue) { this.expectedRevenue = expectedRevenue; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public void setExpectedCloseDate(LocalDate expectedCloseDate) { this.expectedCloseDate = expectedCloseDate; }
    public int getProbability() { return probability; }
    public void setProbability(int probability) { this.probability = probability; }
    public Long getOwnerEmployeeId() { return ownerEmployeeId; }
    public void setOwnerEmployeeId(Long ownerEmployeeId) { this.ownerEmployeeId = ownerEmployeeId; }
    public String getOwnerEmployeeNo() { return ownerEmployeeNo; }
    public void setOwnerEmployeeNo(String ownerEmployeeNo) { this.ownerEmployeeNo = ownerEmployeeNo; }
    public String getOwnerEmployeeName() { return ownerEmployeeName; }
    public void setOwnerEmployeeName(String ownerEmployeeName) { this.ownerEmployeeName = ownerEmployeeName; }
    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
