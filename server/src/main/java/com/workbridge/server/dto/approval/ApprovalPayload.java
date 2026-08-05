package com.workbridge.server.dto.approval;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApprovalPayload(BigDecimal amount, String expenseCategory, LocalDate visitDate, BigDecimal discountRate,
    String description) {
}
