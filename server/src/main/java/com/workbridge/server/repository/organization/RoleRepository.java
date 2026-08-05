package com.workbridge.server.repository.organization;

import com.workbridge.server.domain.organization.RoleRecord;
import com.workbridge.server.mapper.organization.RoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {

    private final RoleMapper mapper;

    public RoleRepository(RoleMapper mapper) {
        this.mapper = mapper;
    }

    public List<RoleRecord> findAll() {
        return mapper.selectAll();
    }

    public Optional<RoleRecord> findById(Long roleId) {
        return Optional.ofNullable(mapper.selectById(roleId));
    }

    public void lockByCode(String roleCode) {
        if (mapper.selectIdByCodeForUpdate(roleCode) == null) {
            throw new IllegalStateException("Required role does not exist: " + roleCode);
        }
    }

    public List<RoleRecord> findByIds(List<Long> roleIds) {
        return roleIds.isEmpty() ? List.of() : mapper.selectByIds(roleIds);
    }

    public List<RoleRecord> findByEmployeeId(Long employeeId) {
        return mapper.selectByEmployeeId(employeeId);
    }

    public boolean touchVersion(Long roleId, Long actorId, long version) {
        return mapper.touchVersion(roleId, actorId, version) == 1;
    }
}
