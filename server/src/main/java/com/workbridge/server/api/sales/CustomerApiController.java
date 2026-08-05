package com.workbridge.server.api.sales;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.dto.sales.CustomerCreateRequest;
import com.workbridge.server.dto.sales.CustomerOptionResponse;
import com.workbridge.server.dto.sales.CustomerResponse;
import com.workbridge.server.dto.sales.CustomerUpdateRequest;
import com.workbridge.server.service.sales.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
@RequestMapping("/api/v1/customers")
@Validated
@Tag(name = "Customers", description = "고객 검색·상세·등록·수정")
public class CustomerApiController {

    private final CustomerService customerService;

    public CustomerApiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'CUSTOMER')")
    @Operation(summary = "고객 조건 검색과 페이징 조회")
    public ApiResponse<PageResponse<CustomerResponse>> search(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String businessRegistrationNo,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) @Positive Long assignedSalesId,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(customerService.search(
                customerName, businessRegistrationNo, contactName, assignedSalesId,
                status, active, page, size));
    }

    @GetMapping("/options")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'CUSTOMER') or @menuPermission.hasRead(authentication, 'OPPORTUNITY') or @menuPermission.hasRead(authentication, 'SALES_ACTIVITY')")
    @Operation(summary = "활성 고객 선택 목록 조회", description = "검색어 기준 최대 50건을 반환합니다.")
    public ApiResponse<List<CustomerOptionResponse>> options(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(customerService.getOptions(keyword));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'CUSTOMER')")
    @Operation(summary = "고객 상세 조회")
    public ApiResponse<CustomerResponse> get(@PathVariable @Positive Long customerId) {
        return ApiResponse.success(customerService.get(customerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'CUSTOMER')")
    @Operation(summary = "고객 등록")
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ApiResponse.success(customerService.create(request), "고객을 등록했습니다.");
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'CUSTOMER')")
    @Operation(summary = "고객 수정")
    public ApiResponse<CustomerResponse> update(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return ApiResponse.success(customerService.update(customerId, request), "고객 정보를 수정했습니다.");
    }
}
