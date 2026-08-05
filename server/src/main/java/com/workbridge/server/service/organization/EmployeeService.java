package com.workbridge.server.service.organization;

import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.audit.MaskingUtils;
import com.workbridge.server.domain.organization.DepartmentRecord;
import com.workbridge.server.domain.organization.EmployeeCredentialRecord;
import com.workbridge.server.domain.organization.EmployeeRecord;
import com.workbridge.server.domain.organization.EmployeeRoleRecord;
import com.workbridge.server.domain.organization.EmployeeStateCommand;
import com.workbridge.server.domain.organization.EmployeeUpdateCommand;
import com.workbridge.server.domain.organization.RoleRecord;
import com.workbridge.server.dto.organization.EmployeeCreateRequest;
import com.workbridge.server.dto.organization.EmployeeResponse;
import com.workbridge.server.dto.organization.EmployeeRoleUpdateRequest;
import com.workbridge.server.dto.organization.EmployeeStateChangeRequest;
import com.workbridge.server.dto.organization.EmployeeUpdateRequest;
import com.workbridge.server.dto.organization.RoleSummaryResponse;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.organization.DepartmentRepository;
import com.workbridge.server.repository.organization.EmployeeRepository;
import com.workbridge.server.repository.organization.RoleRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentEmployee currentEmployee;
    private final AuditService auditService;
    private final Clock clock;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            CurrentEmployee currentEmployee,
            AuditService auditService,
            Clock clock
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentEmployee = currentEmployee;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(
            String keyword,
            Long departmentId,
            Boolean active,
            int page,
            int size
    ) {
        validatePage(page, size);
        String normalizedKeyword = normalizeNullable(keyword);
        List<EmployeeRecord> employees = employeeRepository.findPage(
                normalizedKeyword,
                departmentId,
                active,
                page * size,
                size
        );

        List<Long> employeeIds = employees.stream().map(EmployeeRecord::getEmployeeId).toList();
        Map<Long, List<RoleSummaryResponse>> rolesByEmployee = employeeRepository
                .findRolesByEmployeeIds(employeeIds)
                .stream()
                .collect(Collectors.groupingBy(
                        EmployeeRoleRecord::getEmployeeId,
                        LinkedHashMap::new,
                        Collectors.mapping(RoleSummaryResponse::new, Collectors.toList())
                ));

        List<EmployeeResponse> items = employees.stream()
                .map(employee -> new EmployeeResponse(
                        employee,
                        rolesByEmployee.getOrDefault(employee.getEmployeeId(), List.of())
                ))
                .toList();
        return PageResponse.of(
                items,
                page,
                size,
                employeeRepository.count(normalizedKeyword, departmentId, active)
        );
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Long employeeId) {
        return toResponse(requireEmployee(employeeId));
    }

    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        String employeeNo = request.getEmployeeNo().trim().toUpperCase(Locale.ROOT);
        String loginId = request.getLoginId().trim().toLowerCase(Locale.ROOT);
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String employeeName = request.getEmployeeName().trim();
        DepartmentRecord department = requireActiveDepartment(request.getDepartmentId());
        List<RoleRecord> roles = requireActiveRoles(request.getRoleIds());

        validateUniqueEmployeeFields(employeeNo, loginId, email);
        validateBcryptPasswordLength(request.getPassword());

        EmployeeCredentialRecord employee = new EmployeeCredentialRecord(
                null,
                employeeNo,
                loginId,
                passwordEncoder.encode(request.getPassword()),
                employeeName,
                email,
                department.getDepartmentId(),
                department.getDepartmentName(),
                request.isActive(),
                0,
                null,
                null,
                0
        );

        try {
            employeeRepository.insert(employee, actorId);
            employeeRepository.replaceRoles(
                    employee.getEmployeeId(),
                    roles.stream().map(RoleRecord::getRoleId).toList(),
                    actorId
            );
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "사번, 로그인 ID 또는 이메일이 이미 사용 중입니다.");
        }

        EmployeeResponse created = get(employee.getEmployeeId());
        auditService.record(
                actorId,
                "EMPLOYEE_CREATE",
                "EMPLOYEE",
                String.valueOf(employee.getEmployeeId()),
                null,
                auditData(created)
        );
        return created;
    }

    @Transactional
    public EmployeeResponse update(Long employeeId, EmployeeUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        EmployeeResponse before = get(employeeId);
        DepartmentRecord department = requireActiveDepartment(request.getDepartmentId());
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (employeeRepository.existsByEmailExcludingEmployee(email, employeeId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }

        EmployeeUpdateCommand command = new EmployeeUpdateCommand(
                employeeId,
                request.getEmployeeName().trim(),
                email,
                department.getDepartmentId(),
                actorId,
                request.getVersion()
        );
        try {
            if (!employeeRepository.update(command)) {
                throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }

        EmployeeResponse after = get(employeeId);
        auditService.record(actorId, "EMPLOYEE_UPDATE", "EMPLOYEE", String.valueOf(employeeId),
                auditData(before), auditData(after));
        return after;
    }

    @Transactional
    public EmployeeResponse updateRoles(Long employeeId, EmployeeRoleUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        EmployeeRecord target = requireEmployee(employeeId);
        EmployeeResponse before = toResponse(target);
        List<RoleRecord> requestedRoles = requireActiveRoles(request.getRoleIds());

        boolean hadAdmin = before.getRoles().stream().anyMatch(role -> ADMIN_ROLE_CODE.equals(role.getRoleCode()));
        boolean keepsAdmin = requestedRoles.stream().anyMatch(role -> ADMIN_ROLE_CODE.equals(role.getRoleCode()));
        if (target.isActive() && hadAdmin && !keepsAdmin) {
            // 동일 시점의 관리자 권한 제거 요청을 직렬화해 마지막 관리자 규칙의 경쟁 조건을 막습니다.
            roleRepository.lockByCode(ADMIN_ROLE_CODE);
            if (employeeRepository.countActiveEmployeesByRoleCode(ADMIN_ROLE_CODE) <= 1) {
                throw new BusinessException(ErrorCode.LAST_ACTIVE_ADMIN_REQUIRED);
            }
        }

        if (!employeeRepository.touchVersion(employeeId, actorId, request.getVersion())) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
        employeeRepository.replaceRoles(
                employeeId,
                requestedRoles.stream().map(RoleRecord::getRoleId).toList(),
                actorId
        );

        EmployeeResponse after = get(employeeId);
        auditService.record(actorId, "EMPLOYEE_ROLE_UPDATE", "EMPLOYEE", String.valueOf(employeeId),
                auditData(before), auditData(after));
        return after;
    }

    @Transactional
    public EmployeeResponse activate(Long employeeId, EmployeeStateChangeRequest request) {
        return changeActive(employeeId, request, true);
    }

    @Transactional
    public EmployeeResponse deactivate(Long employeeId, EmployeeStateChangeRequest request) {
        return changeActive(employeeId, request, false);
    }

    @Transactional
    public EmployeeResponse unlock(Long employeeId, EmployeeStateChangeRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        EmployeeRecord target = requireEmployee(employeeId);
        EmployeeResponse before = toResponse(target);
        if (target.getLoginFailCount() == 0 && target.getLockedUntil() == null) {
            return before;
        }

        if (!employeeRepository.unlock(new EmployeeStateCommand(employeeId, actorId, request.getVersion()))) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }

        EmployeeResponse after = get(employeeId);
        auditService.record(actorId, "EMPLOYEE_UNLOCK", "EMPLOYEE", String.valueOf(employeeId),
                auditData(before), auditData(after));
        return after;
    }

    private EmployeeResponse changeActive(
            Long employeeId,
            EmployeeStateChangeRequest request,
            boolean active
    ) {
        Long actorId = currentEmployee.requireEmployeeId();
        EmployeeRecord target = requireEmployee(employeeId);
        EmployeeResponse before = toResponse(target);
        if (target.isActive() == active) {
            return before;
        }

        if (!active && actorId.equals(employeeId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "현재 로그인한 계정은 직접 비활성화할 수 없습니다.");
        }
        if (!active && hasRole(before, ADMIN_ROLE_CODE)) {
            roleRepository.lockByCode(ADMIN_ROLE_CODE);
            if (employeeRepository.countActiveEmployeesByRoleCode(ADMIN_ROLE_CODE) <= 1) {
                throw new BusinessException(ErrorCode.LAST_ACTIVE_ADMIN_REQUIRED);
            }
        }

        if (!employeeRepository.updateActive(
                new EmployeeStateCommand(employeeId, actorId, request.getVersion()),
                active
        )) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
        if (!active) {
            employeeRepository.revokeRefreshTokens(employeeId, LocalDateTime.now(clock));
        }

        EmployeeResponse after = get(employeeId);
        auditService.record(
                actorId,
                active ? "EMPLOYEE_ACTIVATE" : "EMPLOYEE_DEACTIVATE",
                "EMPLOYEE",
                String.valueOf(employeeId),
                auditData(before),
                auditData(after)
        );
        return after;
    }

    private DepartmentRecord requireActiveDepartment(Long departmentId) {
        DepartmentRecord department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
        if (!department.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "활성 부서만 직원에게 지정할 수 있습니다.");
        }
        return department;
    }

    private List<RoleRecord> requireActiveRoles(List<Long> requestedRoleIds) {
        Set<Long> distinctRoleIds = new LinkedHashSet<>(requestedRoleIds == null ? List.of() : requestedRoleIds);
        if (distinctRoleIds.isEmpty() || distinctRoleIds.contains(null)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "역할을 하나 이상 선택해 주세요.");
        }
        List<RoleRecord> roles = new ArrayList<>(roleRepository.findByIds(List.copyOf(distinctRoleIds)));
        roles.sort(Comparator.comparing(RoleRecord::getRoleId));
        if (roles.size() != distinctRoleIds.size() || roles.stream().anyMatch(role -> !role.isActive())) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "선택한 역할 중 사용할 수 없는 항목이 있습니다.");
        }
        return roles;
    }

    private void validateBcryptPasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."
            );
        }
    }

    private void validateUniqueEmployeeFields(String employeeNo, String loginId, String email) {
        if (employeeRepository.existsByEmployeeNo(employeeNo)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 사번입니다.");
        }
        if (employeeRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 로그인 ID입니다.");
        }
        if (employeeRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }
    }

    private EmployeeRecord requireEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private EmployeeResponse toResponse(EmployeeRecord employee) {
        List<RoleSummaryResponse> roles = employeeRepository.findRolesByEmployeeId(employee.getEmployeeId())
                .stream()
                .map(RoleSummaryResponse::new)
                .toList();
        return new EmployeeResponse(employee, roles);
    }

    private boolean hasRole(EmployeeResponse employee, String roleCode) {
        return employee.getRoles().stream().anyMatch(role -> roleCode.equals(role.getRoleCode()));
    }

    private Map<String, Object> auditData(EmployeeResponse employee) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employeeNo", MaskingUtils.maskLoginId(employee.getEmployeeNo()));
        data.put("loginId", MaskingUtils.maskLoginId(employee.getLoginId()));
        data.put("employeeName", "[MASKED]");
        data.put("email", MaskingUtils.maskRejectedValue("email", employee.getEmail()));
        data.put("departmentId", employee.getDepartmentId());
        data.put("active", employee.isActive());
        data.put("loginFailCount", employee.getLoginFailCount());
        data.put("lockedUntil", employee.getLockedUntil());
        data.put("roles", employee.getRoles().stream().map(RoleSummaryResponse::getRoleCode).toList());
        data.put("version", employee.getVersion());
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
