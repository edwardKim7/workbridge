package com.workbridge.server.dto.organization;

import java.util.List;

public class RoleMenuResponse {

    private final RoleSummaryResponse role;
    private final List<RoleMenuPermissionResponse> permissions;

    public RoleMenuResponse(RoleSummaryResponse role, List<RoleMenuPermissionResponse> permissions) {
        this.role = role;
        this.permissions = List.copyOf(permissions);
    }

    public RoleSummaryResponse getRole() { return role; }
    public List<RoleMenuPermissionResponse> getPermissions() { return permissions; }
}
