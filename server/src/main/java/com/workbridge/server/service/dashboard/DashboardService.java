package com.workbridge.server.service.dashboard;

import com.workbridge.server.dto.dashboard.OrganizationDashboardResponse;
import com.workbridge.server.repository.organization.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    public DashboardService(EmployeeRepository employeeRepository, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public OrganizationDashboardResponse getOrganizationSummary() {
        return new OrganizationDashboardResponse(
                employeeRepository.getDashboardStats(LocalDateTime.now(clock))
        );
    }
}
