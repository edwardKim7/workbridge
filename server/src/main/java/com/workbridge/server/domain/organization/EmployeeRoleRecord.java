package com.workbridge.server.domain.organization;

public class EmployeeRoleRecord {

    private Long employeeId;
    private Long roleId;
    private String roleCode;
    private String roleName;

    public EmployeeRoleRecord() {
    }

    public EmployeeRoleRecord(
            Long employeeId,
            Long roleId,
            String roleCode,
            String roleName
    ) {
        this.employeeId = employeeId;
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
