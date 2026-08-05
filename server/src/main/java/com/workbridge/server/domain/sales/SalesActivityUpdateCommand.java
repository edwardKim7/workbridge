package com.workbridge.server.domain.sales;

import java.time.LocalDateTime;

public class SalesActivityUpdateCommand {

    private final Long activityId;
    private final Long opportunityId;
    private final Long employeeId;
    private final SalesActivityType activityType;
    private final String subject;
    private final LocalDateTime activityAt;
    private final String location;
    private final String content;
    private final Long actorId;
    private final long version;

    public SalesActivityUpdateCommand(
            Long activityId,
            Long opportunityId,
            Long employeeId,
            SalesActivityType activityType,
            String subject,
            LocalDateTime activityAt,
            String location,
            String content,
            Long actorId,
            long version
    ) {
        this.activityId = activityId;
        this.opportunityId = opportunityId;
        this.employeeId = employeeId;
        this.activityType = activityType;
        this.subject = subject;
        this.activityAt = activityAt;
        this.location = location;
        this.content = content;
        this.actorId = actorId;
        this.version = version;
    }

    public Long getActivityId() { return activityId; }
    public Long getOpportunityId() { return opportunityId; }
    public Long getEmployeeId() { return employeeId; }
    public SalesActivityType getActivityType() { return activityType; }
    public String getSubject() { return subject; }
    public LocalDateTime getActivityAt() { return activityAt; }
    public String getLocation() { return location; }
    public String getContent() { return content; }
    public Long getActorId() { return actorId; }
    public long getVersion() { return version; }
}
