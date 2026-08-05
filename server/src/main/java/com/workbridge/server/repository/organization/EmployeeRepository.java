package com.workbridge.server.repository.organization;

import com.workbridge.server.domain.organization.EmployeeCredentialRecord;
import com.workbridge.server.domain.organization.EmployeeRecord;
import com.workbridge.server.domain.organization.EmployeeRoleRecord;
import com.workbridge.server.domain.organization.EmployeeStateCommand;
import com.workbridge.server.domain.organization.EmployeeUpdateCommand;
import com.workbridge.server.domain.organization.OrganizationDashboardStats;
import com.workbridge.server.mapper.organization.EmployeeMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final EmployeeMapper mapper;

    public EmployeeRepository(EmployeeMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<EmployeeCredentialRecord> findCredentialByLoginId(String loginId) {
        return Optional.ofNullable(mapper.selectCredentialByLoginId(loginId));
    }

    public List<String> findRoleCodes(Long employeeId) {
        return mapper.selectRoleCodesByEmployeeId(employeeId);
    }

    public Optional<EmployeeRecord> findById(Long employeeId) {
        return Optional.ofNullable(mapper.selectById(employeeId));
    }

    public List<EmployeeRecord> findPage(
            String keyword,
            Long departmentId,
            Boolean active,
            int offset,
            int size
    ) {
        return mapper.selectPage(keyword, departmentId, active, offset, size);
    }

    public long count(String keyword, Long departmentId, Boolean active) {
        return mapper.countPage(keyword, departmentId, active);
    }

    public List<EmployeeRoleRecord> findRolesByEmployeeIds(List<Long> employeeIds) {
        return employeeIds.isEmpty() ? List.of() : mapper.selectRolesByEmployeeIds(employeeIds);
    }

    public List<EmployeeRoleRecord> findRolesByEmployeeId(Long employeeId) {
        return mapper.selectRolesByEmployeeId(employeeId);
    }

    public boolean existsByEmployeeNo(String employeeNo) {
        return mapper.countByEmployeeNo(employeeNo) > 0;
    }

    public boolean existsByLoginId(String loginId) {
        return mapper.countByLoginId(loginId) > 0;
    }

    public boolean existsByEmail(String email) {
        return mapper.countByEmail(email) > 0;
    }

    public boolean existsByEmailExcludingEmployee(String email, Long employeeId) {
        return mapper.countByEmailExcludingEmployee(email, employeeId) > 0;
    }

    public Long insert(EmployeeCredentialRecord employee, Long actorId) {
        mapper.insert(employee, actorId);
        return employee.getEmployeeId();
    }

    public boolean update(EmployeeUpdateCommand command) {
        return mapper.update(command) == 1;
    }

    public boolean updateActive(EmployeeStateCommand command, boolean active) {
        return mapper.updateActive(command, active) == 1;
    }

    public boolean unlock(EmployeeStateCommand command) {
        return mapper.unlock(command) == 1;
    }

    public boolean touchVersion(Long employeeId, Long actorId, long version) {
        return mapper.touchVersion(employeeId, actorId, version) == 1;
    }

    public void replaceRoles(Long employeeId, List<Long> roleIds, Long actorId) {
        mapper.deleteRoles(employeeId);
        mapper.insertRoles(employeeId, roleIds, actorId);
    }

    public void clearExpiredLock(Long employeeId, LocalDateTime now) {
        mapper.clearExpiredLock(employeeId, now);
    }

    public void recordLoginFailure(
            Long employeeId,
            int maxFailures,
            LocalDateTime lockedUntil,
            LocalDateTime now
    ) {
        mapper.recordLoginFailure(employeeId, maxFailures, lockedUntil, now);
    }

    public void recordLoginSuccess(Long employeeId, LocalDateTime now) {
        mapper.recordLoginSuccess(employeeId, now);
    }

    public void revokeRefreshTokens(Long employeeId, LocalDateTime now) {
        mapper.revokeRefreshTokens(employeeId, now);
    }

    public long countActiveEmployeesByRoleCode(String roleCode) {
        return mapper.countActiveEmployeesByRoleCode(roleCode);
    }

    public OrganizationDashboardStats getDashboardStats(LocalDateTime now) {
        return mapper.selectOrganizationDashboardStats(now);
    }
}
