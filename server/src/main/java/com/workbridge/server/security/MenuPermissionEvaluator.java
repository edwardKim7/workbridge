package com.workbridge.server.security;

import com.workbridge.server.repository.organization.MenuRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("menuPermission")
public class MenuPermissionEvaluator {

    private final MenuRepository menuRepository;

    public MenuPermissionEvaluator(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public boolean hasRead(Authentication authentication, String menuCode) {
        return hasPermission(authentication, menuCode, false);
    }

    public boolean hasWrite(Authentication authentication, String menuCode) {
        return hasPermission(authentication, menuCode, true);
    }

    private boolean hasPermission(Authentication authentication, String menuCode, boolean writePermission) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof WorkBridgePrincipal principal)
                || menuCode == null || menuCode.isBlank()) {
            return false;
        }
        return menuRepository.hasPermission(principal.getEmployeeId(), menuCode, writePermission);
    }
}
