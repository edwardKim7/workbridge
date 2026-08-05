package com.workbridge.server.service.organization;

import com.workbridge.server.domain.organization.MenuRecord;
import com.workbridge.server.domain.organization.RoleMenuGrant;
import com.workbridge.server.domain.organization.RoleMenuPermissionRecord;
import com.workbridge.server.domain.organization.RoleRecord;
import com.workbridge.server.dto.organization.MenuResponse;
import com.workbridge.server.dto.organization.RoleMenuPermissionItemRequest;
import com.workbridge.server.dto.organization.RoleMenuPermissionResponse;
import com.workbridge.server.dto.organization.RoleMenuResponse;
import com.workbridge.server.dto.organization.RoleMenuUpdateRequest;
import com.workbridge.server.dto.organization.RoleSummaryResponse;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.organization.MenuRepository;
import com.workbridge.server.repository.organization.RoleRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RoleMenuService {

    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final String PROTECTED_MENU_CODE = "EMPLOYEE_ROLE";
    private static final String DASHBOARD_MENU_CODE = "DASHBOARD";

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final CurrentEmployee currentEmployee;
    private final AuditService auditService;

    public RoleMenuService(
            RoleRepository roleRepository,
            MenuRepository menuRepository,
            CurrentEmployee currentEmployee,
            AuditService auditService
    ) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.currentEmployee = currentEmployee;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<RoleSummaryResponse> getRoles() {
        return roleRepository.findAll().stream().map(RoleSummaryResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus() {
        return menuRepository.findAll().stream().map(MenuResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public RoleMenuResponse getRoleMenus(Long roleId) {
        RoleRecord role = requireRole(roleId);
        List<RoleMenuPermissionResponse> permissions = menuRepository.findPermissionsByRoleId(roleId)
                .stream()
                .map(RoleMenuPermissionResponse::new)
                .toList();
        return new RoleMenuResponse(new RoleSummaryResponse(role), permissions);
    }

    @Transactional
    public RoleMenuResponse updateRoleMenus(Long roleId, RoleMenuUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        RoleRecord role = requireRole(roleId);
        RoleMenuResponse before = getRoleMenus(roleId);
        List<RoleMenuGrant> grants = normalizeAndValidate(role, request.getPermissions());

        if (!roleRepository.touchVersion(roleId, actorId, request.getVersion())) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
        menuRepository.replaceRoleMenus(roleId, grants, actorId);

        RoleMenuResponse after = getRoleMenus(roleId);
        auditService.record(
                actorId,
                "ROLE_MENU_UPDATE",
                "ROLE",
                String.valueOf(roleId),
                auditData(before),
                auditData(after)
        );
        return after;
    }

    private List<RoleMenuGrant> normalizeAndValidate(
            RoleRecord role,
            List<RoleMenuPermissionItemRequest> requestedPermissions
    ) {
        List<MenuRecord> activeMenus = menuRepository.findAll().stream()
                .filter(MenuRecord::isActive)
                .toList();
        Map<Long, MenuRecord> menuById = new HashMap<>();
        for (MenuRecord menu : activeMenus) {
            menuById.put(menu.getMenuId(), menu);
        }

        Map<Long, RoleMenuGrant> grantByMenuId = new LinkedHashMap<>();
        Set<Long> duplicated = new HashSet<>();
        for (RoleMenuPermissionItemRequest permission : requestedPermissions) {
            Long menuId = permission.getMenuId();
            if (!menuById.containsKey(menuId)) {
                throw new BusinessException(ErrorCode.INVALID_MENU_PERMISSION, "존재하지 않거나 비활성화된 메뉴가 포함되어 있습니다.");
            }
            if (grantByMenuId.containsKey(menuId)) {
                duplicated.add(menuId);
            }
            if (permission.isCanWrite() && !permission.isCanRead()) {
                throw new BusinessException(ErrorCode.INVALID_MENU_PERMISSION, "쓰기 권한에는 읽기 권한이 함께 필요합니다.");
            }
            grantByMenuId.put(menuId, new RoleMenuGrant(menuId, permission.isCanRead(), permission.isCanWrite()));
        }
        if (!duplicated.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_MENU_PERMISSION, "동일한 메뉴 권한이 중복되어 있습니다.");
        }

        // 하위 메뉴가 보이면 상위 메뉴도 탐색 경로로 보여야 하므로 읽기 권한을 자동 보완합니다.
        List<RoleMenuGrant> snapshot = new ArrayList<>(grantByMenuId.values());
        for (RoleMenuGrant grant : snapshot) {
            if (!grant.isCanRead()) {
                continue;
            }
            MenuRecord menu = menuById.get(grant.getMenuId());
            Long parentId = menu.getParentMenuId();
            Set<Long> visitedParentIds = new HashSet<>();
            while (parentId != null) {
                if (!visitedParentIds.add(parentId)) {
                    throw new BusinessException(
                            ErrorCode.INTERNAL_SERVER_ERROR,
                            "메뉴 계층에 순환 참조가 있어 권한을 저장할 수 없습니다."
                    );
                }
                MenuRecord parent = menuById.get(parentId);
                if (parent == null) {
                    throw new BusinessException(
                            ErrorCode.INTERNAL_SERVER_ERROR,
                            "활성 하위 메뉴의 상위 메뉴가 비활성 상태이거나 존재하지 않습니다."
                    );
                }
                RoleMenuGrant parentGrant = grantByMenuId.get(parentId);
                if (parentGrant == null || !parentGrant.isCanRead()) {
                    grantByMenuId.put(parentId, new RoleMenuGrant(parentId, true,
                            parentGrant != null && parentGrant.isCanWrite()));
                }
                parentId = parent.getParentMenuId();
            }
        }

        MenuRecord dashboardMenu = activeMenus.stream()
                .filter(menu -> DASHBOARD_MENU_CODE.equals(menu.getMenuCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "기본 대시보드 메뉴를 찾을 수 없습니다."));
        RoleMenuGrant dashboardGrant = grantByMenuId.get(dashboardMenu.getMenuId());
        grantByMenuId.put(
                dashboardMenu.getMenuId(),
                new RoleMenuGrant(
                        dashboardMenu.getMenuId(),
                        true,
                        dashboardGrant != null && dashboardGrant.isCanWrite()
                )
        );

        if (ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
            MenuRecord protectedMenu = activeMenus.stream()
                    .filter(menu -> PROTECTED_MENU_CODE.equals(menu.getMenuCode()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                            "보호 대상 권한 관리 메뉴를 찾을 수 없습니다."));
            RoleMenuGrant protectedGrant = grantByMenuId.get(protectedMenu.getMenuId());
            if (protectedGrant == null || !protectedGrant.isCanRead() || !protectedGrant.isCanWrite()) {
                throw new BusinessException(ErrorCode.PROTECTED_ADMIN_PERMISSION);
            }
        }

        return grantByMenuId.values().stream()
                .filter(grant -> grant.isCanRead() || grant.isCanWrite())
                .sorted(Comparator.comparing(RoleMenuGrant::getMenuId))
                .toList();
    }

    private RoleRecord requireRole(Long roleId) {
        RoleRecord role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        if (!role.isActive()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "비활성 역할은 수정할 수 없습니다.");
        }
        return role;
    }

    private Map<String, Object> auditData(RoleMenuResponse response) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("roleCode", response.getRole().getRoleCode());
        data.put("version", response.getRole().getVersion());
        data.put("permissions", response.getPermissions().stream()
                .filter(permission -> permission.isCanRead() || permission.isCanWrite())
                .map(permission -> Map.of(
                        "menuCode", permission.getMenuCode(),
                        "canRead", permission.isCanRead(),
                        "canWrite", permission.isCanWrite()
                ))
                .toList());
        return data;
    }
}
