package com.workbridge.server.security;

import com.workbridge.server.domain.organization.EmployeeRecord;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.organization.EmployeeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionAccountStatusFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionAccountStatusFilter.class);

    private final EmployeeRepository employeeRepository;
    private final SecurityErrorResponseWriter errorResponseWriter;

    public SessionAccountStatusFilter(
            EmployeeRepository employeeRepository,
            SecurityErrorResponseWriter errorResponseWriter
    ) {
        this.employeeRepository = employeeRepository;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof WorkBridgePrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        final boolean active;
        try {
            active = employeeRepository.findById(principal.getEmployeeId())
                    .map(EmployeeRecord::isActive)
                    .orElse(false);
        } catch (DataAccessException exception) {
            log.error("Account status could not be verified. employeeId={}, path={}",
                    principal.getEmployeeId(), request.getRequestURI(), exception);
            if (isApiRequest(request)) {
                errorResponseWriter.write(request, response, ErrorCode.DATABASE_UNAVAILABLE);
            } else {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
            return;
        }
        if (active) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        if (isApiRequest(request)) {
            errorResponseWriter.write(
                    request,
                    response,
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    "비활성화된 계정입니다. 다시 로그인해 주세요."
            );
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login?inactive");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/")
                || path.equals("/login")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico")
                || path.equals("/api/v1/system/ping")
                || path.equals("/actuator/health")
                || path.equals("/error");
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/");
    }
}
