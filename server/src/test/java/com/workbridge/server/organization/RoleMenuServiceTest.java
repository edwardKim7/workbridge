package com.workbridge.server.organization;

import com.workbridge.server.domain.organization.MenuRecord;
import com.workbridge.server.domain.organization.RoleRecord;
import com.workbridge.server.dto.organization.RoleMenuPermissionItemRequest;
import com.workbridge.server.dto.organization.RoleMenuUpdateRequest;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.organization.MenuRepository;
import com.workbridge.server.repository.organization.RoleRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import com.workbridge.server.service.organization.RoleMenuService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleMenuServiceTest {

    @Test
    void writePermissionWithoutReadPermissionIsRejected() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        CurrentEmployee currentEmployee = mock(CurrentEmployee.class);
        AuditService auditService = mock(AuditService.class);
        RoleMenuService service = new RoleMenuService(
                roleRepository,
                menuRepository,
                currentEmployee,
                auditService
        );

        RoleRecord viewer = new RoleRecord(4L, "VIEWER", "조회 사용자", "조회 전용", true, 0);
        MenuRecord dashboard = new MenuRecord(1L, null, "DASHBOARD", "대시보드", "/web/dashboard", 10, true);
        when(currentEmployee.requireEmployeeId()).thenReturn(1L);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(viewer));
        when(menuRepository.findPermissionsByRoleId(4L)).thenReturn(List.of());
        when(menuRepository.findAll()).thenReturn(List.of(dashboard));

        RoleMenuPermissionItemRequest permission = new RoleMenuPermissionItemRequest();
        permission.setMenuId(1L);
        permission.setCanRead(false);
        permission.setCanWrite(true);
        RoleMenuUpdateRequest request = new RoleMenuUpdateRequest();
        request.setVersion(0);
        request.setPermissions(List.of(permission));

        assertThatThrownBy(() -> service.updateRoleMenus(4L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_MENU_PERMISSION));
    }

    @Test
    void activeChildWithInactiveParentIsRejectedAsMenuConfigurationError() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        CurrentEmployee currentEmployee = mock(CurrentEmployee.class);
        AuditService auditService = mock(AuditService.class);
        RoleMenuService service = new RoleMenuService(
                roleRepository,
                menuRepository,
                currentEmployee,
                auditService
        );

        RoleRecord viewer = new RoleRecord(4L, "VIEWER", "조회 사용자", "조회 전용", true, 0);
        MenuRecord dashboard = new MenuRecord(1L, null, "DASHBOARD", "대시보드", "/web/dashboard", 10, true);
        MenuRecord inactiveParent = new MenuRecord(6L, null, "ADMINISTRATION", "관리", null, 60, false);
        MenuRecord activeChild = new MenuRecord(7L, 6L, "EMPLOYEE_ROLE", "직원·권한", "/web/admin/employees", 10, true);
        when(currentEmployee.requireEmployeeId()).thenReturn(1L);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(viewer));
        when(menuRepository.findPermissionsByRoleId(4L)).thenReturn(List.of());
        when(menuRepository.findAll()).thenReturn(List.of(dashboard, inactiveParent, activeChild));

        RoleMenuPermissionItemRequest permission = new RoleMenuPermissionItemRequest();
        permission.setMenuId(7L);
        permission.setCanRead(true);
        permission.setCanWrite(false);
        RoleMenuUpdateRequest request = new RoleMenuUpdateRequest();
        request.setVersion(0);
        request.setPermissions(List.of(permission));

        assertThatThrownBy(() -> service.updateRoleMenus(4L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR));
    }

}
