package com.workbridge.server.security;

import com.workbridge.server.audit.MaskingUtils;
import com.workbridge.server.service.audit.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);

    private final AuditService auditService;

    public LoginFailureHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String loginId = normalizeLoginId(request.getParameter("loginId"));
        try {
            auditService.record(
                    null,
                    "LOGIN_FAILURE",
                    "LOGIN_ID",
                    MaskingUtils.maskLoginId(loginId),
                    null,
                    Map.of(
                            "loginId", MaskingUtils.maskLoginId(loginId),
                            "result", "FAILURE",
                            "reason", exception.getClass().getSimpleName()
                    )
            );
        } catch (RuntimeException auditException) {
            log.error("Login failure audit could not be recorded. loginId={}", MaskingUtils.maskLoginId(loginId), auditException);
        }
        response.sendRedirect(request.getContextPath() + "/login?error");
    }

    private String normalizeLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = loginId.trim();
        return trimmed.length() > 50 ? trimmed.substring(0, 50) : trimmed;
    }
}
