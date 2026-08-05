package com.workbridge.server.mapper.organization;

import com.workbridge.server.domain.organization.EmployeeCredentialRecord;
import com.workbridge.server.domain.organization.EmployeeRecord;
import com.workbridge.server.domain.organization.EmployeeRoleRecord;
import com.workbridge.server.domain.organization.EmployeeStateCommand;
import com.workbridge.server.domain.organization.EmployeeUpdateCommand;
import com.workbridge.server.domain.organization.OrganizationDashboardStats;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeMapper {

    EmployeeCredentialRecord selectCredentialByLoginId(@Param("loginId") String loginId);

    List<String> selectRoleCodesByEmployeeId(@Param("employeeId") Long employeeId);

    EmployeeRecord selectById(@Param("employeeId") Long employeeId);

    List<EmployeeRecord> selectPage(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countPage(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active
    );

    List<EmployeeRoleRecord> selectRolesByEmployeeIds(@Param("employeeIds") List<Long> employeeIds);

    List<EmployeeRoleRecord> selectRolesByEmployeeId(@Param("employeeId") Long employeeId);

    int countByEmployeeNo(@Param("employeeNo") String employeeNo);

    int countByLoginId(@Param("loginId") String loginId);

    int countByEmail(@Param("email") String email);

    int countByEmailExcludingEmployee(
            @Param("email") String email,
            @Param("employeeId") Long employeeId
    );

    int insert(@Param("employee") EmployeeCredentialRecord employee, @Param("actorId") Long actorId);

    int update(EmployeeUpdateCommand command);

    int updateActive(
            @Param("command") EmployeeStateCommand command,
            @Param("active") boolean active
    );

    int unlock(EmployeeStateCommand command);

    int touchVersion(
            @Param("employeeId") Long employeeId,
            @Param("actorId") Long actorId,
            @Param("version") long version
    );

    int deleteRoles(@Param("employeeId") Long employeeId);

    int insertRoles(
            @Param("employeeId") Long employeeId,
            @Param("roleIds") List<Long> roleIds,
            @Param("actorId") Long actorId
    );

    int clearExpiredLock(
            @Param("employeeId") Long employeeId,
            @Param("now") LocalDateTime now
    );

    int recordLoginFailure(
            @Param("employeeId") Long employeeId,
            @Param("maxFailures") int maxFailures,
            @Param("lockedUntil") LocalDateTime lockedUntil,
            @Param("now") LocalDateTime now
    );

    int recordLoginSuccess(
            @Param("employeeId") Long employeeId,
            @Param("now") LocalDateTime now
    );

    int revokeRefreshTokens(
            @Param("employeeId") Long employeeId,
            @Param("now") LocalDateTime now
    );

    long countActiveEmployeesByRoleCode(@Param("roleCode") String roleCode);

    OrganizationDashboardStats selectOrganizationDashboardStats(@Param("now") LocalDateTime now);
}
