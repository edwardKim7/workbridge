package com.workbridge.server.security;

import com.workbridge.server.audit.MaskingUtils;
import com.workbridge.server.service.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class LogoutAuditSuccessHandler implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LogoutAuditSuccessHandler.class);

    private final AuditService auditService;

    public LogoutAuditSuccessHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof WorkBridgePrincipal principal) {
            try {
                auditService.record(
                        principal.getEmployeeId(),
                        "LOGOUT",
                        "EMPLOYEE",
                        String.valueOf(principal.getEmployeeId()),
                        null,
                        Map.of("loginId", MaskingUtils.maskLoginId(principal.getLoginId()), "result", "SUCCESS")
                );
            } catch (RuntimeException exception) {
                log.error("Logout audit could not be recorded. employeeId={}", principal.getEmployeeId(), exception);
            }
        }
        response.sendRedirect(request.getContextPath() + "/login?logout");
    }
}
