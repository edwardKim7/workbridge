package com.workbridge.server.api.operation;

import com.workbridge.server.api.common.*;
import com.workbridge.server.dto.operation.*;
import com.workbridge.server.service.operation.OperationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/operations")
@PreAuthorize("hasRole('ADMIN') and @menuPermission.hasRead(authentication, 'SYSTEM_OPERATION')")
public class OperationApiController {
    private final OperationService s;

    public OperationApiController(OperationService s) {
        this.s = s;
    }

    @GetMapping("/summary")
    public ApiResponse<OperationSummaryResponse> summary() {
        return ApiResponse.success(s.summary());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<PageResponse<AuditLogResponse>> audits(@RequestParam(required = false)
    String action, @RequestParam(required = false)
    String traceId, @RequestParam(defaultValue = "0")
    int page, @RequestParam(defaultValue = "20")
    int size) {
        return ApiResponse.success(s.audits(action, traceId, page, size));
    }

    @GetMapping("/errors")
    public ApiResponse<PageResponse<SystemErrorResponse>> errors(@RequestParam(required = false)
    String traceId, @RequestParam(required = false)
    Integer httpStatus, @RequestParam(defaultValue = "0")
    int page, @RequestParam(defaultValue = "20")
    int size) {
        return ApiResponse.success(s.errors(traceId, httpStatus, page, size));
    }
}
