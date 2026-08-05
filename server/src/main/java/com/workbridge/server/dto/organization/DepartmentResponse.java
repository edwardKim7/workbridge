package com.workbridge.server.dto.organization;

import com.workbridge.server.domain.organization.DepartmentRecord;

import java.time.LocalDateTime;

public class DepartmentResponse {

    private final Long departmentId;
    private final String departmentCode;
    private final String departmentName;
    private final Long parentDepartmentId;
    private final String parentDepartmentName;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final long version;

    public DepartmentResponse(DepartmentRecord department) {
        this.departmentId = department.getDepartmentId();
        this.departmentCode = department.getDepartmentCode();
        this.departmentName = department.getDepartmentName();
        this.parentDepartmentId = department.getParentDepartmentId();
        this.parentDepartmentName = department.getParentDepartmentName();
        this.active = department.isActive();
        this.createdAt = department.getCreatedAt();
        this.updatedAt = department.getUpdatedAt();
        this.version = department.getVersion();
    }

    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentCode() { return departmentCode; }
    public String getDepartmentName() { return departmentName; }
    public Long getParentDepartmentId() { return parentDepartmentId; }
    public String getParentDepartmentName() { return parentDepartmentName; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
