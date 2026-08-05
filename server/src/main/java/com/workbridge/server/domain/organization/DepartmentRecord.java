package com.workbridge.server.domain.organization;

import java.time.LocalDateTime;

public class DepartmentRecord {

    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private Long parentDepartmentId;
    private String parentDepartmentName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public DepartmentRecord() {
    }

    public DepartmentRecord(
            Long departmentId,
            String departmentCode,
            String departmentName,
            Long parentDepartmentId,
            String parentDepartmentName,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long version
    ) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.parentDepartmentId = parentDepartmentId;
        this.parentDepartmentName = parentDepartmentName;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getParentDepartmentId() {
        return parentDepartmentId;
    }

    public void setParentDepartmentId(Long parentDepartmentId) {
        this.parentDepartmentId = parentDepartmentId;
    }

    public String getParentDepartmentName() {
        return parentDepartmentName;
    }

    public void setParentDepartmentName(String parentDepartmentName) {
        this.parentDepartmentName = parentDepartmentName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
