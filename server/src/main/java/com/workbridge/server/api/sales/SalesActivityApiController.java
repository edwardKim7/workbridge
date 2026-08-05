package com.workbridge.server.api.sales;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.domain.sales.SalesActivityType;
import com.workbridge.server.dto.sales.SalesActivityCreateRequest;
import com.workbridge.server.dto.sales.SalesActivityResponse;
import com.workbridge.server.dto.sales.SalesActivityUpdateRequest;
import com.workbridge.server.service.sales.SalesActivityService;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/activities")
@Validated
@Tag(name = "Sales Activities", description = "고객 방문·통화·회의·이메일 활동 이력")
public class SalesActivityApiController {

    private static final String READ_PERMISSION =
            "@menuPermission.hasRead(authentication, 'SALES_ACTIVITY')";
    private static final String WRITE_PERMISSION =
            "@menuPermission.hasWrite(authentication, 'SALES_ACTIVITY')";

    private final SalesActivityService activityService;

    public SalesActivityApiController(SalesActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    @PreAuthorize(READ_PERMISSION)
    @Operation(summary = "영업활동 조건 검색과 페이징 조회")
    public ApiResponse<PageResponse<SalesActivityResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) @Positive Long opportunityId,
            @RequestParam(required = false) @Positive Long employeeId,
            @RequestParam(required = false) SalesActivityType activityType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(activityService.search(
                keyword, customerId, opportunityId, employeeId, activityType,
                from, to, page, size));
    }

    @GetMapping("/{activityId}")
    @PreAuthorize(READ_PERMISSION)
    @Operation(summary = "영업활동 상세 조회")
    public ApiResponse<SalesActivityResponse> get(@PathVariable @Positive Long activityId) {
        return ApiResponse.success(activityService.get(activityId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(WRITE_PERMISSION)
    @Operation(summary = "영업활동 등록")
    public ApiResponse<SalesActivityResponse> create(
            @Valid @RequestBody SalesActivityCreateRequest request
    ) {
        return ApiResponse.success(activityService.create(request), "영업활동을 등록했습니다.");
    }

    @PutMapping("/{activityId}")
    @PreAuthorize(WRITE_PERMISSION)
    @Operation(summary = "영업활동 수정")
    public ApiResponse<SalesActivityResponse> update(
            @PathVariable @Positive Long activityId,
            @Valid @RequestBody SalesActivityUpdateRequest request
    ) {
        return ApiResponse.success(activityService.update(activityId, request),
                "영업활동을 수정했습니다.");
    }
}
