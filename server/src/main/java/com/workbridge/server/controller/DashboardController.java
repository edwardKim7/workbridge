package com.workbridge.server.controller;

import com.workbridge.server.security.MenuPermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final MenuPermissionEvaluator menuPermissionEvaluator;

    public DashboardController(MenuPermissionEvaluator menuPermissionEvaluator) {
        this.menuPermissionEvaluator = menuPermissionEvaluator;
    }

    @GetMapping({"/web/dashboard", "/web/home"})
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DASHBOARD')")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("activeMenu", "DASHBOARD");
        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("canReadCustomer",
                menuPermissionEvaluator.hasRead(authentication, "CUSTOMER"));
        model.addAttribute("canReadOpportunity",
                menuPermissionEvaluator.hasRead(authentication, "OPPORTUNITY"));
        model.addAttribute("canReadActivity",
                menuPermissionEvaluator.hasRead(authentication, "SALES_ACTIVITY"));
        return "dashboard";
    }
}
