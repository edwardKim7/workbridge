package com.workbridge.server.repository.organization;

import com.workbridge.server.domain.organization.MenuRecord;
import com.workbridge.server.domain.organization.RoleMenuGrant;
import com.workbridge.server.domain.organization.RoleMenuPermissionRecord;
import com.workbridge.server.mapper.organization.MenuMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuRepository {

    private final MenuMapper mapper;

    public MenuRepository(MenuMapper mapper) {
        this.mapper = mapper;
    }

    public List<MenuRecord> findAll() {
        return mapper.selectAll();
    }

    public List<RoleMenuPermissionRecord> findPermissionsByRoleId(Long roleId) {
        return mapper.selectPermissionsByRoleId(roleId);
    }

    public List<RoleMenuPermissionRecord> findReadableMenusByEmployeeId(Long employeeId) {
        return mapper.selectReadableMenusByEmployeeId(employeeId);
    }

    public boolean hasPermission(Long employeeId, String menuCode, boolean writePermission) {
        return mapper.countEmployeePermission(employeeId, menuCode, writePermission) > 0;
    }

    public void replaceRoleMenus(Long roleId, List<RoleMenuGrant> grants, Long actorId) {
        mapper.deleteRoleMenus(roleId);
        if (!grants.isEmpty()) {
            mapper.insertRoleMenus(roleId, grants, actorId);
        }
    }
}
