package com.workbridge.server.dto.approval;

import java.util.List;

public record ApprovalDetailResponse(ApprovalResponse approval, List<ApprovalHistoryResponse> histories,
    boolean canEdit, boolean canRequest, boolean canApprove, boolean canReject, boolean canCancel) {
}
