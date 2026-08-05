package com.workbridge.server.domain.approval;
import java.time.LocalDateTime;
public class ApprovalRecord {
    private Long approvalId;
    private String approvalNo;
    private ApprovalType approvalType;
    private String businessType;
    private Long businessId;
    private String businessName;
    private Long requesterId;
    private String requesterName;
    private Long approverId;
    private String approverName;
    private String title;
    private String requestPayload;
    private ApprovalStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;
    public Long getApprovalId() {
        return approvalId;
    }
    public void setApprovalId(Long v) {
        approvalId = v;
    }
    public String getApprovalNo() {
        return approvalNo;
    }
    public void setApprovalNo(String v) {
        approvalNo = v;
    }
    public ApprovalType getApprovalType() {
        return approvalType;
    }
    public void setApprovalType(ApprovalType v) {
        approvalType = v;
    }
    public String getBusinessType() {
        return businessType;
    }
    public void setBusinessType(String v) {
        businessType = v;
    }
    public Long getBusinessId() {
        return businessId;
    }
    public void setBusinessId(Long v) {
        businessId = v;
    }
    public String getBusinessName() {
        return businessName;
    }
    public void setBusinessName(String v) {
        businessName = v;
    }
    public Long getRequesterId() {
        return requesterId;
    }
    public void setRequesterId(Long v) {
        requesterId = v;
    }
    public String getRequesterName() {
        return requesterName;
    }
    public void setRequesterName(String v) {
        requesterName = v;
    }
    public Long getApproverId() {
        return approverId;
    }
    public void setApproverId(Long v) {
        approverId = v;
    }
    public String getApproverName() {
        return approverName;
    }
    public void setApproverName(String v) {
        approverName = v;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String v) {
        title = v;
    }
    public String getRequestPayload() {
        return requestPayload;
    }
    public void setRequestPayload(String v) {
        requestPayload = v;
    }
    public ApprovalStatus getStatus() {
        return status;
    }
    public void setStatus(ApprovalStatus v) {
        status = v;
    }
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
    public void setRequestedAt(LocalDateTime v) {
        requestedAt = v;
    }
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    public void setProcessedAt(LocalDateTime v) {
        processedAt = v;
    }
    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }
    public void setCanceledAt(LocalDateTime v) {
        canceledAt = v;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime v) {
        createdAt = v;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime v) {
        updatedAt = v;
    }
    public long getVersion() {
        return version;
    }
    public void setVersion(long v) {
        version = v;
    }
}
