package com.workbridge.server.service.sales;

import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.sales.SalesEmployeeRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.security.WorkBridgePrincipal;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SalesAccessPolicy {

    private static final String ADMIN = "ADMIN";
    private static final String SALES = "SALES";

    private final CurrentEmployee currentEmployee;
    private final SalesEmployeeRepository salesEmployeeRepository;

    public SalesAccessPolicy(
            CurrentEmployee currentEmployee,
            SalesEmployeeRepository salesEmployeeRepository
    ) {
        this.currentEmployee = currentEmployee;
        this.salesEmployeeRepository = salesEmployeeRepository;
    }

    public Long resolveAssigneeForCreate(Long requestedEmployeeId) {
        WorkBridgePrincipal principal = currentEmployee.requirePrincipal();
        if (principal.hasRole(ADMIN)) {
            return requireActiveSalesEmployee(requestedEmployeeId);
        }
        if (principal.hasRole(SALES)) {
            if (requestedEmployeeId != null
                    && !Objects.equals(requestedEmployeeId, principal.getEmployeeId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED,
                        "영업 담당자는 자신에게만 업무를 배정할 수 있습니다.");
            }
            return requireActiveSalesEmployee(principal.getEmployeeId());
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    public Long resolveAssigneeForUpdate(Long requestedEmployeeId, Long currentOwnerEmployeeId) {
        WorkBridgePrincipal principal = currentEmployee.requirePrincipal();
        if (principal.hasRole(ADMIN)) {
            Long candidate = requestedEmployeeId == null ? currentOwnerEmployeeId : requestedEmployeeId;
            return requireActiveSalesEmployee(candidate);
        }
        if (principal.hasRole(SALES)) {
            requireCanModifyAssigned(currentOwnerEmployeeId);
            if (requestedEmployeeId != null
                    && !Objects.equals(requestedEmployeeId, principal.getEmployeeId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED,
                        "영업 담당자는 업무 소유자를 다른 직원으로 변경할 수 없습니다.");
            }
            return requireActiveSalesEmployee(principal.getEmployeeId());
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    public void requireCanModifyAssigned(Long ownerEmployeeId) {
        WorkBridgePrincipal principal = currentEmployee.requirePrincipal();
        if (principal.hasRole(ADMIN)) {
            return;
        }
        if (principal.hasRole(SALES)
                && Objects.equals(principal.getEmployeeId(), ownerEmployeeId)) {
            return;
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED,
                "담당 영업사원 또는 관리자만 수정할 수 있습니다.");
    }

    public Long dashboardOwnerFilter() {
        WorkBridgePrincipal principal = currentEmployee.requirePrincipal();
        if (principal.hasRole(ADMIN)) {
            return null;
        }
        return principal.hasRole(SALES) ? principal.getEmployeeId() : null;
    }

    public boolean isAdmin() {
        return currentEmployee.requirePrincipal().hasRole(ADMIN);
    }

    private Long requireActiveSalesEmployee(Long employeeId) {
        if (!salesEmployeeRepository.isActiveSalesEmployee(employeeId)) {
            throw new BusinessException(ErrorCode.INVALID_SALES_ASSIGNEE);
        }
        return employeeId;
    }
}
