package com.workbridge.server.security;

import com.workbridge.server.audit.MaskingUtils;
import com.workbridge.server.service.audit.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final SavedRequestAwareAuthenticationSuccessHandler delegate;
    private final AuditService auditService;

    public LoginSuccessHandler(AuditService auditService) {
        this.auditService = auditService;
        this.delegate = new SavedRequestAwareAuthenticationSuccessHandler();
        this.delegate.setDefaultTargetUrl("/web/dashboard");
        this.delegate.setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof WorkBridgePrincipal principal) {
            try {
                auditService.record(
                        principal.getEmployeeId(),
                        "LOGIN_SUCCESS",
                        "EMPLOYEE",
                        String.valueOf(principal.getEmployeeId()),
                        null,
                        Map.of("loginId", MaskingUtils.maskLoginId(principal.getLoginId()), "result", "SUCCESS")
                );
            } catch (RuntimeException exception) {
                log.error("Login success audit could not be recorded. employeeId={}", principal.getEmployeeId(), exception);
            }
        }
        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
