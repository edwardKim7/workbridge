package com.workbridge.server.dto.approval;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ApprovalTransitionRequest(@PositiveOrZero long version, @Size(max = 1000) String opinion) {
}
