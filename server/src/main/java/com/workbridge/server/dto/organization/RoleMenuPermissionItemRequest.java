package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.NotNull;

public class RoleMenuPermissionItemRequest {

    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;
    private boolean canRead;
    private boolean canWrite;

    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public boolean isCanRead() { return canRead; }
    public void setCanRead(boolean canRead) { this.canRead = canRead; }
    public boolean isCanWrite() { return canWrite; }
    public void setCanWrite(boolean canWrite) { this.canWrite = canWrite; }
}
