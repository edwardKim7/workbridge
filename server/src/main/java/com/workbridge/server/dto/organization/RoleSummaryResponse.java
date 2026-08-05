package com.workbridge.server.dto.organization;

import com.workbridge.server.domain.organization.EmployeeRoleRecord;
import com.workbridge.server.domain.organization.RoleRecord;

public class RoleSummaryResponse {

    private final Long roleId;
    private final String roleCode;
    private final String roleName;
    private final String description;
    private final boolean active;
    private final long version;

    public RoleSummaryResponse(RoleRecord role) {
        this.roleId = role.getRoleId();
        this.roleCode = role.getRoleCode();
        this.roleName = role.getRoleName();
        this.description = role.getDescription();
        this.active = role.isActive();
        this.version = role.getVersion();
    }

    public RoleSummaryResponse(EmployeeRoleRecord role) {
        this.roleId = role.getRoleId();
        this.roleCode = role.getRoleCode();
        this.roleName = role.getRoleName();
        this.description = null;
        this.active = true;
        this.version = 0;
    }

    public Long getRoleId() { return roleId; }
    public String getRoleCode() { return roleCode; }
    public String getRoleName() { return roleName; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
}
