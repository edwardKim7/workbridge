package com.workbridge.server.dto.organization;

import com.workbridge.server.domain.organization.RoleMenuPermissionRecord;

public class RoleMenuPermissionResponse {

    private final Long menuId;
    private final Long parentMenuId;
    private final String menuCode;
    private final String menuName;
    private final String menuPath;
    private final int sortOrder;
    private final boolean canRead;
    private final boolean canWrite;

    public RoleMenuPermissionResponse(RoleMenuPermissionRecord permission) {
        this.menuId = permission.getMenuId();
        this.parentMenuId = permission.getParentMenuId();
        this.menuCode = permission.getMenuCode();
        this.menuName = permission.getMenuName();
        this.menuPath = permission.getMenuPath();
        this.sortOrder = permission.getSortOrder();
        this.canRead = permission.isCanRead();
        this.canWrite = permission.isCanWrite();
    }

    public Long getMenuId() { return menuId; }
    public Long getParentMenuId() { return parentMenuId; }
    public String getMenuCode() { return menuCode; }
    public String getMenuName() { return menuName; }
    public String getMenuPath() { return menuPath; }
    public int getSortOrder() { return sortOrder; }
    public boolean isCanRead() { return canRead; }
    public boolean isCanWrite() { return canWrite; }
}
