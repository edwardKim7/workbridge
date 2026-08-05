package com.workbridge.server.api.sales;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import com.workbridge.server.dto.sales.OpportunityCreateRequest;
import com.workbridge.server.dto.sales.OpportunityOptionResponse;
import com.workbridge.server.dto.sales.OpportunityResponse;
import com.workbridge.server.dto.sales.OpportunityUpdateRequest;
import com.workbridge.server.service.sales.SalesOpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/opportunities")
@Validated
@Tag(name = "Sales Opportunities", description = "영업기회 검색·상세·등록·수정")
public class SalesOpportunityApiController {

    private final SalesOpportunityService opportunityService;

    public SalesOpportunityApiController(SalesOpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'OPPORTUNITY')")
    @Operation(summary = "영업기회 조건 검색과 페이징 조회")
    public ApiResponse<PageResponse<OpportunityResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) OpportunityStage stage,
            @RequestParam(required = false) OpportunityStatus status,
            @RequestParam(required = false) @Positive Long ownerEmployeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closeDateTo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(opportunityService.search(
                keyword, customerId, stage, status, ownerEmployeeId,
                closeDateFrom, closeDateTo, page, size));
    }

    @GetMapping("/options")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'OPPORTUNITY') or @menuPermission.hasRead(authentication, 'SALES_ACTIVITY')")
    @Operation(summary = "고객별 영업기회 선택 목록 조회")
    public ApiResponse<List<OpportunityOptionResponse>> options(@RequestParam @Positive Long customerId) {
        return ApiResponse.success(opportunityService.getOptionsByCustomer(customerId));
    }

    @GetMapping("/{opportunityId}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'OPPORTUNITY')")
    @Operation(summary = "영업기회 상세 조회")
    public ApiResponse<OpportunityResponse> get(@PathVariable @Positive Long opportunityId) {
        return ApiResponse.success(opportunityService.get(opportunityId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'OPPORTUNITY')")
    @Operation(summary = "영업기회 등록")
    public ApiResponse<OpportunityResponse> create(@Valid @RequestBody OpportunityCreateRequest request) {
        return ApiResponse.success(opportunityService.create(request), "영업기회를 등록했습니다.");
    }

    @PutMapping("/{opportunityId}")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'OPPORTUNITY')")
    @Operation(summary = "영업기회 수정")
    public ApiResponse<OpportunityResponse> update(
            @PathVariable @Positive Long opportunityId,
            @Valid @RequestBody OpportunityUpdateRequest request
    ) {
        return ApiResponse.success(opportunityService.update(opportunityId, request),
                "영업기회 정보를 수정했습니다.");
    }
}
