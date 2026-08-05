package com.workbridge.server.domain.approval;

import java.time.LocalDateTime;

public class ApprovalHistoryRecord {
    private Long approvalHistoryId;
    private Long approvalId;
    private ApprovalStatus fromStatus;
    private ApprovalStatus toStatus;
    private ApprovalAction action;
    private Long actorId;
    private String actorName;
    private String opinion;
    private String traceId;
    private LocalDateTime actedAt;
    public Long getApprovalHistoryId() {
        return approvalHistoryId;
    }
    public void setApprovalHistoryId(Long v) {
        approvalHistoryId = v;
    }
    public Long getApprovalId() {
        return approvalId;
    }
    public void setApprovalId(Long v) {
        approvalId = v;
    }
    public ApprovalStatus getFromStatus() {
        return fromStatus;
    }
    public void setFromStatus(ApprovalStatus v) {
        fromStatus = v;
    }
    public ApprovalStatus getToStatus() {
        return toStatus;
    }
    public void setToStatus(ApprovalStatus v) {
        toStatus = v;
    }
    public ApprovalAction getAction() {
        return action;
    }
    public void setAction(ApprovalAction v) {
        action = v;
    }
    public Long getActorId() {
        return actorId;
    }
    public void setActorId(Long v) {
        actorId = v;
    }
    public String getActorName() {
        return actorName;
    }
    public void setActorName(String v) {
        actorName = v;
    }
    public String getOpinion() {
        return opinion;
    }
    public void setOpinion(String v) {
        opinion = v;
    }
    public String getTraceId() {
        return traceId;
    }
    public void setTraceId(String v) {
        traceId = v;
    }
    public LocalDateTime getActedAt() {
        return actedAt;
    }
    public void setActedAt(LocalDateTime v) {
        actedAt = v;
    }
}
