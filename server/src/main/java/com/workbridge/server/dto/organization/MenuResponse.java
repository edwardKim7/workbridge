package com.workbridge.server.dto.organization;

import com.workbridge.server.domain.organization.MenuRecord;

public class MenuResponse {

    private final Long menuId;
    private final Long parentMenuId;
    private final String menuCode;
    private final String menuName;
    private final String menuPath;
    private final int sortOrder;
    private final boolean active;

    public MenuResponse(MenuRecord menu) {
        this.menuId = menu.getMenuId();
        this.parentMenuId = menu.getParentMenuId();
        this.menuCode = menu.getMenuCode();
        this.menuName = menu.getMenuName();
        this.menuPath = menu.getMenuPath();
        this.sortOrder = menu.getSortOrder();
        this.active = menu.isActive();
    }

    public Long getMenuId() { return menuId; }
    public Long getParentMenuId() { return parentMenuId; }
    public String getMenuCode() { return menuCode; }
    public String getMenuName() { return menuName; }
    public String getMenuPath() { return menuPath; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
}
