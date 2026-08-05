package com.workbridge.server.api.integration;

import com.workbridge.server.api.common.*;
import com.workbridge.server.domain.integration.*;
import com.workbridge.server.dto.integration.*;
import com.workbridge.server.service.integration.ErpIntegrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interfaces")
@PreAuthorize("hasRole('ADMIN')")
public class InterfaceApiController {
    private final ErpIntegrationService service;

    public InterfaceApiController(ErpIntegrationService s) {
        service = s;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'INTERFACE_OPERATION')")
    public ApiResponse<PageResponse<InterfaceResponse>> search(@RequestParam(required = false)
    InterfaceStatus status, @RequestParam(required = false)
    String traceId, @RequestParam(defaultValue = "0")
    int page, @RequestParam(defaultValue = "20")
    int size) {
        return ApiResponse.success(service.search(status, traceId, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'INTERFACE_OPERATION')")
    public ApiResponse<InterfaceDetailResponse> get(@PathVariable
    Long id) {
        return ApiResponse.success(service.get(id));
    }

    @PostMapping("/erp/{businessId}/send")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'INTERFACE_OPERATION')")
    public ApiResponse<InterfaceResponse> send(@PathVariable
    Long businessId, @RequestBody(required = false)
    InterfaceSendRequest r) {
        return ApiResponse.success(service.sendApprovedOpportunity(businessId, r == null ? ErpSimulationMode.NORMAL : r.effectiveMode()));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'INTERFACE_OPERATION')")
    public ApiResponse<InterfaceResponse> retry(@PathVariable
    Long id, @RequestBody(required = false)
    InterfaceSendRequest r) {
        return ApiResponse.success(service.retry(id, r == null ? ErpSimulationMode.NORMAL : r.effectiveMode()));
    }
}
