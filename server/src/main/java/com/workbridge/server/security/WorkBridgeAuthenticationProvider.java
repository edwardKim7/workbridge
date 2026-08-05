package com.workbridge.server.security;

import com.workbridge.server.service.auth.EmployeeAuthenticationService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class WorkBridgeAuthenticationProvider implements AuthenticationProvider {

    private final EmployeeAuthenticationService authenticationService;

    public WorkBridgeAuthenticationProvider(EmployeeAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = authentication.getCredentials() == null
                ? ""
                : authentication.getCredentials().toString();

        WorkBridgePrincipal principal = authenticationService.authenticate(loginId, password);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
