package com.workbridge.server.dto.organization;

import com.workbridge.server.domain.organization.EmployeeRecord;

import java.time.LocalDateTime;
import java.util.List;

public class EmployeeResponse {

    private final Long employeeId;
    private final String employeeNo;
    private final String loginId;
    private final String employeeName;
    private final String email;
    private final Long departmentId;
    private final String departmentName;
    private final boolean active;
    private final int loginFailCount;
    private final LocalDateTime lockedUntil;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final long version;
    private final List<RoleSummaryResponse> roles;

    public EmployeeResponse(EmployeeRecord employee, List<RoleSummaryResponse> roles) {
        this.employeeId = employee.getEmployeeId();
        this.employeeNo = employee.getEmployeeNo();
        this.loginId = employee.getLoginId();
        this.employeeName = employee.getEmployeeName();
        this.email = employee.getEmail();
        this.departmentId = employee.getDepartmentId();
        this.departmentName = employee.getDepartmentName();
        this.active = employee.isActive();
        this.loginFailCount = employee.getLoginFailCount();
        this.lockedUntil = employee.getLockedUntil();
        this.lastLoginAt = employee.getLastLoginAt();
        this.createdAt = employee.getCreatedAt();
        this.updatedAt = employee.getUpdatedAt();
        this.version = employee.getVersion();
        this.roles = List.copyOf(roles);
    }

    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public String getLoginId() { return loginId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmail() { return email; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public boolean isActive() { return active; }
    public int getLoginFailCount() { return loginFailCount; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public List<RoleSummaryResponse> getRoles() { return roles; }
}
