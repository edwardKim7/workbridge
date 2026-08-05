package com.workbridge.server.domain.organization;

public class RoleRecord {

    private Long roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private boolean active;
    private long version;

    public RoleRecord() {
    }

    public RoleRecord(
            Long roleId,
            String roleCode,
            String roleName,
            String description,
            boolean active,
            long version
    ) {
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
        this.active = active;
        this.version = version;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
