package com.workbridge.server.api.sales;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.sales.SalesDashboardResponse;
import com.workbridge.server.service.sales.SalesDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/dashboard")
@Tag(name = "Sales Dashboard", description = "고객·영업기회 상태별 현황")
public class SalesDashboardApiController {

    private final SalesDashboardService dashboardService;

    public SalesDashboardApiController(SalesDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DASHBOARD')")
    @Operation(summary = "역할 범위를 적용한 영업 현황 요약")
    public ApiResponse<SalesDashboardResponse> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }
}
