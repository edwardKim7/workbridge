package com.workbridge.server.dto.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ErpSalesPayload(Long approvalId, String approvalNo, Long opportunityId, String opportunityNo,
    String opportunityName, Long customerId, String businessRegistrationNo, String customerName, String stage,
    BigDecimal confirmedRevenue, String ownerEmployeeNo, String ownerEmployeeName, long sourceVersion,
    LocalDateTime approvedAt) {
}
