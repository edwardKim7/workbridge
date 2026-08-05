package com.workbridge.mockerp;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockErpController {

    private final MockErpService mockErpService;

    public MockErpController(MockErpService mockErpService) {
        this.mockErpService = mockErpService;
    }

    @PostMapping("/api/v1/erp/sales")
    public ErpSalesResponse send(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Mock-Mode", defaultValue = "NORMAL") MockMode mode,
            @Valid @RequestBody ErpSalesRequest request) {
        return mockErpService.process(idempotencyKey, request, mode);
    }

    @GetMapping("/api/v1/admin/records")
    public List<Map<String, Object>> records() {
        return mockErpService.records();
    }

    @DeleteMapping("/api/v1/admin/records")
    public void clear() {
        mockErpService.clear();
    }

    @ExceptionHandler(MockErpException.class)
    ResponseEntity<Map<String, Object>> handleMockErpException(MockErpException exception) {
        return ResponseEntity.status(exception.status())
                .body(Map.of(
                        "code", exception.getMessage(),
                        "message", "의도적으로 재현한 Mock ERP 오류입니다."));
    }
}
