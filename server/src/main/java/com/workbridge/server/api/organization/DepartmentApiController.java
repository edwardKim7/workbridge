package com.workbridge.server.api.organization;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.dto.organization.DepartmentCreateRequest;
import com.workbridge.server.dto.organization.DepartmentResponse;
import com.workbridge.server.dto.organization.DepartmentUpdateRequest;
import com.workbridge.server.service.organization.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@Validated
@Tag(name = "Departments", description = "부서 조회·등록·수정")
public class DepartmentApiController {

    private final DepartmentService departmentService;

    public DepartmentApiController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "부서 검색·페이징 조회")
    public ApiResponse<PageResponse<DepartmentResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(departmentService.search(keyword, active, page, size));
    }

    @GetMapping("/options")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "활성 부서 선택 목록 조회")
    public ApiResponse<List<DepartmentResponse>> options() {
        return ApiResponse.success(departmentService.getActiveOptions());
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "부서 상세 조회")
    public ApiResponse<DepartmentResponse> get(@PathVariable Long departmentId) {
        return ApiResponse.success(departmentService.get(departmentId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "부서 등록")
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return ApiResponse.success(departmentService.create(request), "부서를 등록했습니다.");
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "부서 수정")
    public ApiResponse<DepartmentResponse> update(
            @PathVariable Long departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        return ApiResponse.success(departmentService.update(departmentId, request), "부서를 수정했습니다.");
    }
}
