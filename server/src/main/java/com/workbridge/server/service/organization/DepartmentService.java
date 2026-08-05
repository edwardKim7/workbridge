package com.workbridge.server.service.organization;

import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.domain.organization.DepartmentRecord;
import com.workbridge.server.domain.organization.DepartmentUpdateCommand;
import com.workbridge.server.dto.organization.DepartmentCreateRequest;
import com.workbridge.server.dto.organization.DepartmentResponse;
import com.workbridge.server.dto.organization.DepartmentUpdateRequest;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.organization.DepartmentRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DepartmentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DepartmentRepository departmentRepository;
    private final CurrentEmployee currentEmployee;
    private final AuditService auditService;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            CurrentEmployee currentEmployee,
            AuditService auditService
    ) {
        this.departmentRepository = departmentRepository;
        this.currentEmployee = currentEmployee;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> search(String keyword, Boolean active, int page, int size) {
        validatePage(page, size);
        String normalizedKeyword = normalizeNullable(keyword);
        int offset = page * size;
        List<DepartmentResponse> items = departmentRepository
                .findPage(normalizedKeyword, active, offset, size)
                .stream()
                .map(DepartmentResponse::new)
                .toList();
        return PageResponse.of(items, page, size, departmentRepository.count(normalizedKeyword, active));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(Long departmentId) {
        return new DepartmentResponse(requireDepartment(departmentId));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveOptions() {
        return departmentRepository.findAll().stream()
                .filter(DepartmentRecord::isActive)
                .map(DepartmentResponse::new)
                .toList();
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        String code = request.getDepartmentCode().trim().toUpperCase(Locale.ROOT);
        String name = request.getDepartmentName().trim();

        if (departmentRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 부서코드입니다.");
        }
        validateParent(null, request.getParentDepartmentId());

        DepartmentRecord department = new DepartmentRecord(
                null,
                code,
                name,
                request.getParentDepartmentId(),
                null,
                request.isActive(),
                null,
                null,
                0
        );
        try {
            departmentRepository.insert(department, actorId);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 부서코드입니다.");
        }

        DepartmentRecord created = requireDepartment(department.getDepartmentId());
        auditService.record(actorId, "DEPARTMENT_CREATE", "DEPARTMENT",
                String.valueOf(created.getDepartmentId()), null, auditData(created));
        return new DepartmentResponse(created);
    }

    @Transactional
    public DepartmentResponse update(Long departmentId, DepartmentUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        DepartmentRecord before = requireDepartment(departmentId);
        validateParent(departmentId, request.getParentDepartmentId());

        if (before.isActive() && !request.isActive()
                && (departmentRepository.hasActiveEmployees(departmentId)
                || departmentRepository.hasActiveChildren(departmentId))) {
            throw new BusinessException(ErrorCode.DEPARTMENT_IN_USE);
        }

        DepartmentUpdateCommand command = new DepartmentUpdateCommand(
                departmentId,
                request.getDepartmentName().trim(),
                request.getParentDepartmentId(),
                request.isActive(),
                actorId,
                request.getVersion()
        );
        if (!departmentRepository.update(command)) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }

        DepartmentRecord after = requireDepartment(departmentId);
        auditService.record(actorId, "DEPARTMENT_UPDATE", "DEPARTMENT",
                String.valueOf(departmentId), auditData(before), auditData(after));
        return new DepartmentResponse(after);
    }

    private DepartmentRecord requireDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private void validateParent(Long departmentId, Long parentDepartmentId) {
        if (parentDepartmentId == null) {
            return;
        }
        DepartmentRecord parent = requireDepartment(parentDepartmentId);
        if (!parent.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상위 부서는 활성 상태여야 합니다.");
        }
        if (departmentId == null) {
            return;
        }
        if (departmentId.equals(parentDepartmentId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "자기 자신을 상위 부서로 지정할 수 없습니다.");
        }

        Map<Long, Long> parents = new HashMap<>();
        for (DepartmentRecord department : departmentRepository.findAll()) {
            parents.put(department.getDepartmentId(), department.getParentDepartmentId());
        }
        Set<Long> visited = new HashSet<>();
        Long cursor = parentDepartmentId;
        while (cursor != null && visited.add(cursor)) {
            if (departmentId.equals(cursor)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "하위 부서를 상위 부서로 지정할 수 없습니다.");
            }
            cursor = parents.get(cursor);
        }
    }

    private Map<String, Object> auditData(DepartmentRecord department) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("departmentCode", department.getDepartmentCode());
        data.put("departmentName", department.getDepartmentName());
        data.put("parentDepartmentId", department.getParentDepartmentId());
        data.put("active", department.isActive());
        data.put("version", department.getVersion());
        return data;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상, size는 1~100으로 입력해 주세요.");
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
