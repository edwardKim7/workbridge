package com.workbridge.server.dto.organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RoleMenuUpdateRequest {

    @NotNull(message = "메뉴 권한 목록은 필수입니다.")
    @Valid
    private List<RoleMenuPermissionItemRequest> permissions = new ArrayList<>();

    @Min(value = 0, message = "버전은 0 이상이어야 합니다.")
    private long version;

    public List<RoleMenuPermissionItemRequest> getPermissions() { return permissions; }
    public void setPermissions(List<RoleMenuPermissionItemRequest> permissions) { this.permissions = permissions; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
