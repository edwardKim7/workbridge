package com.workbridge.server.api.organization;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.organization.MenuResponse;
import com.workbridge.server.dto.organization.RoleMenuResponse;
import com.workbridge.server.dto.organization.RoleMenuUpdateRequest;
import com.workbridge.server.dto.organization.RoleSummaryResponse;
import com.workbridge.server.service.organization.RoleMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Roles and menus", description = "고정 역할과 메뉴 접근 권한 관리")
public class RoleMenuApiController {

    private final RoleMenuService roleMenuService;

    public RoleMenuApiController(RoleMenuService roleMenuService) {
        this.roleMenuService = roleMenuService;
    }

    @GetMapping("/roles")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "역할 목록 조회")
    public ApiResponse<List<RoleSummaryResponse>> getRoles() {
        return ApiResponse.success(roleMenuService.getRoles());
    }

    @GetMapping("/menus")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "메뉴 목록 조회")
    public ApiResponse<List<MenuResponse>> getMenus() {
        return ApiResponse.success(roleMenuService.getMenus());
    }

    @GetMapping("/roles/{roleId}/menus")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "역할별 메뉴 권한 조회")
    public ApiResponse<RoleMenuResponse> getRoleMenus(@PathVariable Long roleId) {
        return ApiResponse.success(roleMenuService.getRoleMenus(roleId));
    }

    @PutMapping("/roles/{roleId}/menus")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "역할별 메뉴 권한 변경")
    public ApiResponse<RoleMenuResponse> updateRoleMenus(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleMenuUpdateRequest request
    ) {
        return ApiResponse.success(roleMenuService.updateRoleMenus(roleId, request), "역할별 메뉴 권한을 변경했습니다.");
    }
}
