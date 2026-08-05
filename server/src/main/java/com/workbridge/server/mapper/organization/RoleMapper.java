package com.workbridge.server.mapper.organization;

import com.workbridge.server.domain.organization.RoleRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper {

    List<RoleRecord> selectAll();

    RoleRecord selectById(@Param("roleId") Long roleId);

    Long selectIdByCodeForUpdate(@Param("roleCode") String roleCode);

    List<RoleRecord> selectByIds(@Param("roleIds") List<Long> roleIds);

    List<RoleRecord> selectByEmployeeId(@Param("employeeId") Long employeeId);

    int touchVersion(
            @Param("roleId") Long roleId,
            @Param("actorId") Long actorId,
            @Param("version") long version
    );
}
