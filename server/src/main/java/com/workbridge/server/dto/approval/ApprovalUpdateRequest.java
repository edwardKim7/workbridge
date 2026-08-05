package com.workbridge.server.dto.approval;

import com.workbridge.server.domain.approval.ApprovalType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ApprovalUpdateRequest(
        @NotNull ApprovalType approvalType,
        @NotNull @Positive Long businessId,
        @NotNull @Positive Long approverId,
        @NotBlank @Size(max=200) String title,
        @DecimalMin(value="0.00") BigDecimal amount,
        @Size(max=100) String expenseCategory,
        LocalDate visitDate,
        @DecimalMin(value="0.00") @DecimalMax(value="100.00") BigDecimal discountRate,
        @Size(max=2000) String description,
        @PositiveOrZero long version
) {}
