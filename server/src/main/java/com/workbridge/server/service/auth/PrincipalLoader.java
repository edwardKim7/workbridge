package com.workbridge.server.service.auth;

import com.workbridge.server.domain.organization.EmployeeRecord;
import com.workbridge.server.exception.*;
import com.workbridge.server.repository.organization.EmployeeRepository;
import com.workbridge.server.security.WorkBridgePrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PrincipalLoader {
    private final EmployeeRepository repository;

    public PrincipalLoader(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WorkBridgePrincipal load(Long id) {
        EmployeeRecord e = repository.findById(id).orElseThrow(()->new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED));
        if (!e.isActive()) throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "비활성 계정입니다.");
        List<String> roles = repository.findRoleCodes(id);
        if (roles.isEmpty()) throw new BusinessException(ErrorCode.ACCESS_DENIED);
        return new WorkBridgePrincipal(e.getEmployeeId(), e.getEmployeeNo(), e.getLoginId(), e.getEmployeeName(), e.getEmail(), e.getDepartmentId(), e.getDepartmentName(), roles);
    }
}
