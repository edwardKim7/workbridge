package com.workbridge.server.service.security;

import com.workbridge.server.domain.organization.RoleMenuPermissionRecord;
import com.workbridge.server.dto.web.NavigationMenuResponse;
import com.workbridge.server.repository.organization.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NavigationMenuService {

    private static final Set<String> IMPLEMENTED_MENU_CODES = Set.of(
            "DASHBOARD", "SALES_SUPPORT", "CUSTOMER", "OPPORTUNITY", "SALES_ACTIVITY",
            "ADMINISTRATION", "EMPLOYEE_ROLE"
    );

    private final MenuRepository menuRepository;

    public NavigationMenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public List<NavigationMenuResponse> getReadableMenuTree(Long employeeId) {
        List<RoleMenuPermissionRecord> permissions = menuRepository.findReadableMenusByEmployeeId(employeeId).stream()
                .filter(permission -> IMPLEMENTED_MENU_CODES.contains(permission.getMenuCode()))
                .toList();
        Map<Long, MutableMenuNode> nodes = new LinkedHashMap<>();

        for (RoleMenuPermissionRecord permission : permissions) {
            nodes.put(permission.getMenuId(), new MutableMenuNode(permission));
        }

        List<MutableMenuNode> roots = new ArrayList<>();
        for (MutableMenuNode node : nodes.values()) {
            if (node.parentMenuId != null && nodes.containsKey(node.parentMenuId)) {
                nodes.get(node.parentMenuId).children.add(node);
            } else {
                roots.add(node);
            }
        }

        roots.sort(Comparator.comparingInt(node -> node.sortOrder));
        return roots.stream().map(MutableMenuNode::toResponse).toList();
    }

    private static class MutableMenuNode {
        private final Long menuId;
        private final Long parentMenuId;
        private final String menuCode;
        private final String menuName;
        private final String menuPath;
        private final int sortOrder;
        private final boolean canWrite;
        private final List<MutableMenuNode> children = new ArrayList<>();

        private MutableMenuNode(RoleMenuPermissionRecord permission) {
            this.menuId = permission.getMenuId();
            this.parentMenuId = permission.getParentMenuId();
            this.menuCode = permission.getMenuCode();
            this.menuName = permission.getMenuName();
            this.menuPath = permission.getMenuPath();
            this.sortOrder = permission.getSortOrder();
            this.canWrite = permission.isCanWrite();
        }

        private NavigationMenuResponse toResponse() {
            children.sort(Comparator.comparingInt(node -> node.sortOrder));
            return new NavigationMenuResponse(
                    menuId,
                    parentMenuId,
                    menuCode,
                    menuName,
                    menuPath,
                    sortOrder,
                    canWrite,
                    children.stream().map(MutableMenuNode::toResponse).toList()
            );
        }
    }
}
