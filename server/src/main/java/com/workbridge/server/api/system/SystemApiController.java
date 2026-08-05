package com.workbridge.server.api.system;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.system.SystemPingResponse;
import com.workbridge.server.service.system.SystemStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "서버와 데이터베이스 연결 확인 API")
public class SystemApiController {

    private final SystemStatusService systemStatusService;

    public SystemApiController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/ping")
    @Operation(summary = "서버와 DB 연결 확인")
    public ResponseEntity<ApiResponse<SystemPingResponse>> ping() {
        return ResponseEntity.ok(ApiResponse.success(systemStatusService.getPingStatus()));
    }
}
