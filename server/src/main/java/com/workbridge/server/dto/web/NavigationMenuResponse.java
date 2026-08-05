package com.workbridge.server.dto.web;

import java.util.List;

public class NavigationMenuResponse {

    private final Long menuId;
    private final Long parentMenuId;
    private final String menuCode;
    private final String menuName;
    private final String menuPath;
    private final int sortOrder;
    private final boolean canWrite;
    private final List<NavigationMenuResponse> children;

    public NavigationMenuResponse(
            Long menuId,
            Long parentMenuId,
            String menuCode,
            String menuName,
            String menuPath,
            int sortOrder,
            boolean canWrite,
            List<NavigationMenuResponse> children
    ) {
        this.menuId = menuId;
        this.parentMenuId = parentMenuId;
        this.menuCode = menuCode;
        this.menuName = menuName;
        this.menuPath = menuPath;
        this.sortOrder = sortOrder;
        this.canWrite = canWrite;
        this.children = List.copyOf(children);
    }

    public Long getMenuId() { return menuId; }
    public Long getParentMenuId() { return parentMenuId; }
    public String getMenuCode() { return menuCode; }
    public String getMenuName() { return menuName; }
    public String getMenuPath() { return menuPath; }
    public int getSortOrder() { return sortOrder; }
    public boolean isCanWrite() { return canWrite; }
    public List<NavigationMenuResponse> getChildren() { return children; }
}
