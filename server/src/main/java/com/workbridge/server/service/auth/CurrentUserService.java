package com.workbridge.server.service.auth;

import com.workbridge.server.domain.organization.RoleRecord;
import com.workbridge.server.dto.auth.CurrentUserResponse;
import com.workbridge.server.dto.organization.RoleSummaryResponse;
import com.workbridge.server.repository.organization.RoleRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.security.WorkBridgePrincipal;
import com.workbridge.server.service.security.NavigationMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CurrentUserService {

    private final CurrentEmployee currentEmployee;
    private final RoleRepository roleRepository;
    private final NavigationMenuService navigationMenuService;

    public CurrentUserService(
            CurrentEmployee currentEmployee,
            RoleRepository roleRepository,
            NavigationMenuService navigationMenuService
    ) {
        this.currentEmployee = currentEmployee;
        this.roleRepository = roleRepository;
        this.navigationMenuService = navigationMenuService;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        WorkBridgePrincipal principal = currentEmployee.requirePrincipal();
        List<RoleSummaryResponse> roles = roleRepository.findByEmployeeId(principal.getEmployeeId()).stream()
                .map(RoleSummaryResponse::new)
                .toList();
        return new CurrentUserResponse(
                principal.getEmployeeId(),
                principal.getEmployeeNo(),
                principal.getLoginId(),
                principal.getEmployeeName(),
                principal.getEmail(),
                principal.getDepartmentId(),
                principal.getDepartmentName(),
                roles,
                navigationMenuService.getReadableMenuTree(principal.getEmployeeId())
        );
    }
}
