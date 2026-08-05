package com.workbridge.server.repository.organization;

import com.workbridge.server.domain.organization.DepartmentRecord;
import com.workbridge.server.domain.organization.DepartmentUpdateCommand;
import com.workbridge.server.mapper.organization.DepartmentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DepartmentRepository {

    private final DepartmentMapper mapper;

    public DepartmentRepository(DepartmentMapper mapper) {
        this.mapper = mapper;
    }

    public List<DepartmentRecord> findPage(String keyword, Boolean active, int offset, int size) {
        return mapper.selectPage(keyword, active, offset, size);
    }

    public long count(String keyword, Boolean active) {
        return mapper.countPage(keyword, active);
    }

    public Optional<DepartmentRecord> findById(Long departmentId) {
        return Optional.ofNullable(mapper.selectById(departmentId));
    }

    public List<DepartmentRecord> findAll() {
        return mapper.selectAll();
    }

    public boolean existsByCode(String departmentCode) {
        return mapper.countByCode(departmentCode) > 0;
    }

    public boolean hasActiveEmployees(Long departmentId) {
        return mapper.countActiveEmployees(departmentId) > 0;
    }

    public boolean hasActiveChildren(Long departmentId) {
        return mapper.countActiveChildren(departmentId) > 0;
    }

    public Long insert(DepartmentRecord department, Long actorId) {
        mapper.insert(department, actorId);
        return department.getDepartmentId();
    }

    public boolean update(DepartmentUpdateCommand command) {
        return mapper.update(command) == 1;
    }
}
