package com.workbridge.server.domain.sales;

import java.time.LocalDateTime;

public class SalesActivityRecord {

    private Long activityId;
    private Long customerId;
    private String customerName;
    private Long opportunityId;
    private String opportunityNo;
    private String opportunityName;
    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private SalesActivityType activityType;
    private String subject;
    private LocalDateTime activityAt;
    private String location;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public SalesActivityRecord() {
    }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityNo() { return opportunityNo; }
    public void setOpportunityNo(String opportunityNo) { this.opportunityNo = opportunityNo; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public SalesActivityType getActivityType() { return activityType; }
    public void setActivityType(SalesActivityType activityType) { this.activityType = activityType; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public LocalDateTime getActivityAt() { return activityAt; }
    public void setActivityAt(LocalDateTime activityAt) { this.activityAt = activityAt; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
