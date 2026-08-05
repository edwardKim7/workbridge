package com.workbridge.server.mapper.approval;

import com.workbridge.server.domain.approval.*;
import com.workbridge.server.dto.approval.ApproverOptionResponse;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ApprovalMapper {
    List<ApprovalRecord> selectPage(@Param("scope") String scope, @Param("actorId") Long actorId,
        @Param("status") ApprovalStatus status, @Param("approvalType") ApprovalType approvalType,
        @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("scope") String scope, @Param("actorId") Long actorId,
        @Param("status") ApprovalStatus status, @Param("approvalType") ApprovalType approvalType,
        @Param("keyword") String keyword);
    ApprovalRecord selectById(@Param("approvalId") Long approvalId);
    List<ApprovalHistoryRecord> selectHistories(@Param("approvalId") Long approvalId);
    List<ApproverOptionResponse> selectActiveApprovers();
    int countActiveApprover(@Param("employeeId") Long employeeId);
    int insert(@Param("approval") ApprovalRecord approval, @Param("actorId") Long actorId);
    int updateDraft(ApprovalUpdateCommand command);
    int transitionDraft(@Param("approvalId") Long approvalId, @Param("requesterId") Long requesterId,
        @Param("targetStatus") ApprovalStatus targetStatus, @Param("actorId") Long actorId,
        @Param("version") long version, @Param("now") LocalDateTime now);
    int processRequested(@Param("approvalId") Long approvalId, @Param("approverId") Long approverId,
        @Param("targetStatus") ApprovalStatus targetStatus, @Param("actorId") Long actorId,
        @Param("version") long version, @Param("now") LocalDateTime now);
    int insertHistory(@Param("approvalId") Long approvalId, @Param("fromStatus") ApprovalStatus fromStatus,
        @Param("toStatus") ApprovalStatus toStatus, @Param("action") ApprovalAction action,
        @Param("actorId") Long actorId, @Param("opinion") String opinion, @Param("traceId") String traceId,
        @Param("actedAt") LocalDateTime actedAt);
    List<ApprovalRecord> selectRecent(@Param("limit") int limit);
}
