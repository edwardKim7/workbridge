package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.SalesActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SalesActivityUpdateRequest {

    @Positive(message = "영업기회 ID는 1 이상이어야 합니다.")
    private Long opportunityId;

    @Positive(message = "활동 담당자 ID는 1 이상이어야 합니다.")
    private Long employeeId;

    @NotNull(message = "활동 유형은 필수입니다.")
    private SalesActivityType activityType;

    @NotBlank(message = "활동 제목은 필수입니다.")
    @Size(max = 200, message = "활동 제목은 200자 이하여야 합니다.")
    private String subject;

    @NotNull(message = "활동 일시는 필수입니다.")
    private LocalDateTime activityAt;

    @Size(max = 300, message = "장소는 300자 이하여야 합니다.")
    private String location;

    @Size(max = 4000, message = "활동 내용은 4000자 이하여야 합니다.")
    private String content;

    @NotNull(message = "버전은 필수입니다.")
    @PositiveOrZero(message = "버전은 0 이상이어야 합니다.")
    private Long version;

    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
