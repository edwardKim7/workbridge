package com.workbridge.server.api.auth;

import com.workbridge.server.api.common.ApiResponse;
import com.workbridge.server.dto.auth.*;
import com.workbridge.server.service.auth.MobileAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class MobileAuthApiController {
    private final MobileAuthService service;

    public MobileAuthApiController(MobileAuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ApiResponse<MobileAuthResponse> login(@Valid
    @RequestBody
    MobileLoginRequest r) {
        return ApiResponse.success(service.login(r), "모바일 로그인에 성공했습니다.");
    }

    @PostMapping("/refresh")
    public ApiResponse<MobileAuthResponse> refresh(@Valid
    @RequestBody
    TokenRefreshRequest r) {
        return ApiResponse.success(service.refresh(r), "토큰을 갱신했습니다.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid
    @RequestBody
    MobileLogoutRequest r) {
        service.logout(r);
        return ApiResponse.success(null, "로그아웃했습니다.");
    }
}
