package com.workbridge.server.api.approval;

import com.workbridge.server.api.common.*;
import com.workbridge.server.domain.approval.*;
import com.workbridge.server.dto.approval.*;
import com.workbridge.server.service.approval.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalApiController {
    private final ApprovalService service;

    public ApprovalApiController(ApprovalService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'APPROVAL')")
    public ApiResponse<PageResponse<ApprovalResponse>> search(@RequestParam(required = false)
    ApprovalView view, @RequestParam(required = false)
    ApprovalStatus status, @RequestParam(required = false)
    ApprovalType approvalType, @RequestParam(required = false)
    String keyword, @RequestParam(defaultValue = "0")
    int page, @RequestParam(defaultValue = "20")
    int size) {
        return ApiResponse.success(service.search(view, status, approvalType, keyword, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> get(@PathVariable
    Long id) {
        return ApiResponse.success(service.get(id));
    }

    @GetMapping("/approvers")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'APPROVAL')")
    public ApiResponse<List<ApproverOptionResponse>> approvers() {
        return ApiResponse.success(service.approvers());
    }

    @PostMapping
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> create(@Valid
    @RequestBody
    ApprovalCreateRequest r) {
        return ApiResponse.success(service.create(r), "결재 초안을 등록했습니다.");
    }

    @PutMapping("/{id}")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> update(@PathVariable
    Long id, @Valid
    @RequestBody
    ApprovalUpdateRequest r) {
        return ApiResponse.success(service.update(id, r));
    }

    @PostMapping("/{id}/request")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> request(@PathVariable
    Long id, @Valid
    @RequestBody
    ApprovalTransitionRequest r) {
        return ApiResponse.success(service.request(id, r), "결재를 요청했습니다.");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> approve(@PathVariable
    Long id, @Valid
    @RequestBody
    ApprovalTransitionRequest r) {
        return ApiResponse.success(service.approve(id, r), "승인 처리했습니다.");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> reject(@PathVariable
    Long id, @Valid
    @RequestBody
    ApprovalTransitionRequest r) {
        return ApiResponse.success(service.reject(id, r), "반려 처리했습니다.");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@menuPermission.hasWrite(authentication, 'APPROVAL')")
    public ApiResponse<ApprovalDetailResponse> cancel(@PathVariable
    Long id, @Valid
    @RequestBody
    ApprovalTransitionRequest r) {
        return ApiResponse.success(service.cancel(id, r), "초안을 취소했습니다.");
    }
}
