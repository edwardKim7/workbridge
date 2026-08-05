package com.workbridge.server.api.migration;

import com.workbridge.server.api.common.*;
import com.workbridge.server.domain.migration.MigrationBatchStatus;
import com.workbridge.server.dto.migration.*;
import com.workbridge.server.service.migration.CustomerMigrationService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/migrations")
@PreAuthorize("hasRole('ADMIN')")
public class MigrationApiController {
    private final CustomerMigrationService service;

    public MigrationApiController(CustomerMigrationService s) {
        service = s;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DATA_MIGRATION')")
    public ApiResponse<PageResponse<MigrationBatchResponse>> search(@RequestParam(required = false)
    MigrationBatchStatus status, @RequestParam(defaultValue = "0")
    int page, @RequestParam(defaultValue = "20")
    int size) {
        return ApiResponse.success(service.search(status, page, size));
    }

    @PostMapping(value = "/customers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'DATA_MIGRATION')")
    public ApiResponse<MigrationDetailResponse> upload(@RequestPart("file")
    MultipartFile file) {
        return ApiResponse.success(service.migrate(file), "고객 데이터 이관을 완료했습니다.");
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DATA_MIGRATION')")
    public ApiResponse<MigrationDetailResponse> get(@PathVariable
    Long id) {
        return ApiResponse.success(service.get(id));
    }

    @GetMapping("/{id}/errors")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DATA_MIGRATION')")
    public ApiResponse<?> errors(@PathVariable
    Long id) {
        return ApiResponse.success(service.get(id).errors());
    }

    @GetMapping(value = "/{id}/errors.csv", produces = "text/csv;charset=UTF-8")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DATA_MIGRATION')")
    public ResponseEntity<byte[]> csv(@PathVariable
    Long id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=workbridge-migration-errors-" + id + ".csv").body(service.errorCsv(id));
    }
}
