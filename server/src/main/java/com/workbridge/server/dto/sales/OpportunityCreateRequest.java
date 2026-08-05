package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OpportunityCreateRequest {

    @NotNull(message = "고객 ID는 필수입니다.")
    @Positive(message = "고객 ID는 1 이상이어야 합니다.")
    private Long customerId;

    @NotBlank(message = "영업기회명은 필수입니다.")
    @Size(max = 200, message = "영업기회명은 200자 이하여야 합니다.")
    private String opportunityName;

    @NotNull(message = "영업단계는 필수입니다.")
    private OpportunityStage stage;

    @NotNull(message = "예상매출은 필수입니다.")
    @DecimalMin(value = "0.00", message = "예상매출은 0 이상이어야 합니다.")
    @Digits(integer = 16, fraction = 2, message = "예상매출은 정수 16자리, 소수 2자리 이하여야 합니다.")
    private BigDecimal expectedRevenue;

    @NotNull(message = "예상 종료일은 필수입니다.")
    private LocalDate expectedCloseDate;

    @NotNull(message = "수주확률은 필수입니다.")
    @Min(value = 0, message = "수주확률은 0 이상이어야 합니다.")
    @Max(value = 100, message = "수주확률은 100 이하여야 합니다.")
    private Integer probability;

    @Positive(message = "담당 영업사원 ID는 1 이상이어야 합니다.")
    private Long ownerEmployeeId;

    @Size(max = 2000, message = "설명은 2000자 이하여야 합니다.")
    private String description;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public OpportunityStage getStage() { return stage; }
    public void setStage(OpportunityStage stage) { this.stage = stage; }
    public BigDecimal getExpectedRevenue() { return expectedRevenue; }
    public void setExpectedRevenue(BigDecimal expectedRevenue) { this.expectedRevenue = expectedRevenue; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; }
    public void setExpectedCloseDate(LocalDate expectedCloseDate) { this.expectedCloseDate = expectedCloseDate; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public Long getOwnerEmployeeId() { return ownerEmployeeId; }
    public void setOwnerEmployeeId(Long ownerEmployeeId) { this.ownerEmployeeId = ownerEmployeeId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
