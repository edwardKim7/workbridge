package com.workbridge.server.api.organization;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.dto.organization.EmployeeCreateRequest;
import com.workbridge.server.dto.organization.EmployeeResponse;
import com.workbridge.server.dto.organization.EmployeeRoleUpdateRequest;
import com.workbridge.server.dto.organization.EmployeeStateChangeRequest;
import com.workbridge.server.dto.organization.EmployeeUpdateRequest;
import com.workbridge.server.service.organization.EmployeeService;
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

@RestController
@RequestMapping("/api/v1/employees")
@Validated
@Tag(name = "Employees", description = "직원 계정·역할·상태 관리")
public class EmployeeApiController {

    private final EmployeeService employeeService;

    public EmployeeApiController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 검색·페이징 조회")
    public ApiResponse<PageResponse<EmployeeResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(employeeService.search(keyword, departmentId, active, page, size));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 상세·역할 조회")
    public ApiResponse<EmployeeResponse> get(@PathVariable Long employeeId) {
        return ApiResponse.success(employeeService.get(employeeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 계정 등록")
    public ApiResponse<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ApiResponse.success(employeeService.create(request), "직원 계정을 등록했습니다.");
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 기본정보 수정")
    public ApiResponse<EmployeeResponse> update(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        return ApiResponse.success(employeeService.update(employeeId, request), "직원 정보를 수정했습니다.");
    }

    @PutMapping("/{employeeId}/roles")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 역할 목록 교체")
    public ApiResponse<EmployeeResponse> updateRoles(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeRoleUpdateRequest request
    ) {
        return ApiResponse.success(employeeService.updateRoles(employeeId, request), "직원 역할을 변경했습니다.");
    }

    @PostMapping("/{employeeId}/activate")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 계정 활성화")
    public ApiResponse<EmployeeResponse> activate(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeStateChangeRequest request
    ) {
        return ApiResponse.success(employeeService.activate(employeeId, request), "직원 계정을 활성화했습니다.");
    }

    @PostMapping("/{employeeId}/deactivate")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "직원 계정 비활성화")
    public ApiResponse<EmployeeResponse> deactivate(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeStateChangeRequest request
    ) {
        return ApiResponse.success(employeeService.deactivate(employeeId, request), "직원 계정을 비활성화했습니다.");
    }

    @PostMapping("/{employeeId}/unlock")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'EMPLOYEE_ROLE')")
    @Operation(summary = "로그인 잠금 해제")
    public ApiResponse<EmployeeResponse> unlock(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeStateChangeRequest request
    ) {
        return ApiResponse.success(employeeService.unlock(employeeId, request), "로그인 잠금을 해제했습니다.");
    }
}
