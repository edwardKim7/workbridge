package com.workbridge.server.api.sales;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.sales.SalesEmployeeOptionResponse;
import com.workbridge.server.service.sales.SalesReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "Sales References", description = "영업지원 화면 선택용 기준 데이터")
public class SalesReferenceApiController {

    private final SalesReferenceService referenceService;

    public SalesReferenceApiController(SalesReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping("/employees/options")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'CUSTOMER') or @menuPermission.hasRead(authentication, 'OPPORTUNITY') or @menuPermission.hasRead(authentication, 'SALES_ACTIVITY')")
    @Operation(summary = "활성 영업 담당자 선택 목록 조회")
    public ApiResponse<List<SalesEmployeeOptionResponse>> salesEmployees() {
        return ApiResponse.success(referenceService.getActiveSalesEmployees());
    }
}
