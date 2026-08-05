package com.workbridge.server.api.auth;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.auth.CurrentUserResponse;
import com.workbridge.server.service.auth.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "웹 세션 로그인 사용자 정보")
public class AuthApiController {

    private final CurrentUserService currentUserService;

    public AuthApiController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "현재 로그인 사용자 조회")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(currentUserService.getCurrentUser());
    }
}
