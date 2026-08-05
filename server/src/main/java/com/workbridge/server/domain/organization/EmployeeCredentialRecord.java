package com.workbridge.server.domain.organization;

import java.time.LocalDateTime;

public class EmployeeCredentialRecord {

    private Long employeeId;
    private String employeeNo;
    private String loginId;
    private String passwordHash;
    private String employeeName;
    private String email;
    private Long departmentId;
    private String departmentName;
    private boolean active;
    private int loginFailCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private long version;

    public EmployeeCredentialRecord() {
    }

    public EmployeeCredentialRecord(
            Long employeeId,
            String employeeNo,
            String loginId,
            String passwordHash,
            String employeeName,
            String email,
            Long departmentId,
            String departmentName,
            boolean active,
            int loginFailCount,
            LocalDateTime lockedUntil,
            LocalDateTime lastLoginAt,
            long version
    ) {
        this.employeeId = employeeId;
        this.employeeNo = employeeNo;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.employeeName = employeeName;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.active = active;
        this.loginFailCount = loginFailCount;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.version = version;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
