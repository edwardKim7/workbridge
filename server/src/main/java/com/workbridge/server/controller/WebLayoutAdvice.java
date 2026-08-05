package com.workbridge.server.controller;

import com.workbridge.server.config.WorkBridgeProperties;
import com.workbridge.server.security.WorkBridgePrincipal;
import com.workbridge.server.service.security.NavigationMenuService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(basePackages = "com.workbridge.server.controller")
public class WebLayoutAdvice {

    private final WorkBridgeProperties properties;
    private final NavigationMenuService navigationMenuService;

    public WebLayoutAdvice(
            WorkBridgeProperties properties,
            NavigationMenuService navigationMenuService
    ) {
        this.properties = properties;
        this.navigationMenuService = navigationMenuService;
    }

    @ModelAttribute
    public void addLayoutAttributes(Model model, Authentication authentication) {
        model.addAttribute("appName", properties.getName());
        model.addAttribute("appVersion", properties.getVersion());

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof WorkBridgePrincipal principal) {
            model.addAttribute("currentUser", principal);
            model.addAttribute("navigationMenus",
                    navigationMenuService.getReadableMenuTree(principal.getEmployeeId()));
        }
    }
}
