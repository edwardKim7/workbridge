package com.workbridge.server.domain.organization;

public class RoleMenuGrant {

    private final Long menuId;
    private final boolean canRead;
    private final boolean canWrite;

    public RoleMenuGrant(
            Long menuId,
            boolean canRead,
            boolean canWrite
    ) {
        this.menuId = menuId;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public Long getMenuId() {
        return menuId;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

}
