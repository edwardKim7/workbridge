package com.workbridge.server.mapper.organization;

import com.workbridge.server.domain.organization.MenuRecord;
import com.workbridge.server.domain.organization.RoleMenuGrant;
import com.workbridge.server.domain.organization.RoleMenuPermissionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MenuMapper {

    List<MenuRecord> selectAll();

    List<RoleMenuPermissionRecord> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    List<RoleMenuPermissionRecord> selectReadableMenusByEmployeeId(@Param("employeeId") Long employeeId);

    int countEmployeePermission(
            @Param("employeeId") Long employeeId,
            @Param("menuCode") String menuCode,
            @Param("writePermission") boolean writePermission
    );

    int deleteRoleMenus(@Param("roleId") Long roleId);

    int insertRoleMenus(
            @Param("roleId") Long roleId,
            @Param("grants") List<RoleMenuGrant> grants,
            @Param("actorId") Long actorId
    );
}
