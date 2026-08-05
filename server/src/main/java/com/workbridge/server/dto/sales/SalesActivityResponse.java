package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.SalesActivityRecord;
import com.workbridge.server.domain.sales.SalesActivityType;

import java.time.LocalDateTime;

public class SalesActivityResponse {

    private final Long activityId;
    private final Long customerId;
    private final String customerName;
    private final Long opportunityId;
    private final String opportunityNo;
    private final String opportunityName;
    private final Long employeeId;
    private final String employeeNo;
    private final String employeeName;
    private final SalesActivityType activityType;
    private final String subject;
    private final LocalDateTime activityAt;
    private final String location;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final long version;

    public SalesActivityResponse(SalesActivityRecord activity) {
        this.activityId = activity.getActivityId();
        this.customerId = activity.getCustomerId();
        this.customerName = activity.getCustomerName();
        this.opportunityId = activity.getOpportunityId();
        this.opportunityNo = activity.getOpportunityNo();
        this.opportunityName = activity.getOpportunityName();
        this.employeeId = activity.getEmployeeId();
        this.employeeNo = activity.getEmployeeNo();
        this.employeeName = activity.getEmployeeName();
        this.activityType = activity.getActivityType();
        this.subject = activity.getSubject();
        this.activityAt = activity.getActivityAt();
        this.location = activity.getLocation();
        this.content = activity.getContent();
        this.createdAt = activity.getCreatedAt();
        this.updatedAt = activity.getUpdatedAt();
        this.version = activity.getVersion();
    }

    public Long getActivityId() { return activityId; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getOpportunityId() { return opportunityId; }
    public String getOpportunityNo() { return opportunityNo; }
    public String getOpportunityName() { return opportunityName; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public SalesActivityType getActivityType() { return activityType; }
    public String getSubject() { return subject; }
    public LocalDateTime getActivityAt() { return activityAt; }
    public String getLocation() { return location; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
