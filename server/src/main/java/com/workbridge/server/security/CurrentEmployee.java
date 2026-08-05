package com.workbridge.server.security;

import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentEmployee {

    public WorkBridgePrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof WorkBridgePrincipal principal)) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        return principal;
    }

    public Long requireEmployeeId() {
        return requirePrincipal().getEmployeeId();
    }
}
