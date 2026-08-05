package com.workbridge.server.domain.organization;

public class RoleMenuPermissionRecord {

    private Long menuId;
    private Long parentMenuId;
    private String menuCode;
    private String menuName;
    private String menuPath;
    private int sortOrder;
    private boolean canRead;
    private boolean canWrite;

    public RoleMenuPermissionRecord() {
    }

    public RoleMenuPermissionRecord(
            Long menuId,
            Long parentMenuId,
            String menuCode,
            String menuName,
            String menuPath,
            int sortOrder,
            boolean canRead,
            boolean canWrite
    ) {
        this.menuId = menuId;
        this.parentMenuId = parentMenuId;
        this.menuCode = menuCode;
        this.menuName = menuName;
        this.menuPath = menuPath;
        this.sortOrder = sortOrder;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Long getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(Long parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuPath() {
        return menuPath;
    }

    public void setMenuPath(String menuPath) {
        this.menuPath = menuPath;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

}
