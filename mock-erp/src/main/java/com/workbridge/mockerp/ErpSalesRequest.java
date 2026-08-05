package com.workbridge.mockerp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ErpSalesRequest(
        @NotNull Long approvalId,
        @NotBlank String approvalNo,
        @NotNull Long opportunityId,
        @NotBlank String opportunityNo,
        @NotBlank String opportunityName,
        @NotNull Long customerId,
        @NotBlank String businessRegistrationNo,
        @NotBlank String customerName,
        @NotBlank String stage,
        @NotNull BigDecimal confirmedRevenue,
        @NotBlank String ownerEmployeeNo,
        @NotBlank String ownerEmployeeName,
        long sourceVersion,
        LocalDateTime approvedAt) {}
