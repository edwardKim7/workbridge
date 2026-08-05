package com.workbridge.server.api.dashboard;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.dashboard.OrganizationDashboardResponse;
import com.workbridge.server.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "역할별 업무 대시보드 기반")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DASHBOARD')")
    @Operation(summary = "조직·권한 현황 요약")
    public ApiResponse<OrganizationDashboardResponse> getSummary() {
        return ApiResponse.success(dashboardService.getOrganizationSummary());
    }
}
