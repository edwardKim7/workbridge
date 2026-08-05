package com.workbridge.server.controller;

import com.workbridge.server.security.MenuPermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrganizationAdminController {

    private final MenuPermissionEvaluator menuPermissionEvaluator;

    public OrganizationAdminController(MenuPermissionEvaluator menuPermissionEvaluator) {
        this.menuPermissionEvaluator = menuPermissionEvaluator;
    }

    @GetMapping("/web/admin/employees")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    public String employees(Model model, Authentication authentication) {
        addCommonAttributes(model, authentication, "employees", "직원 관리");
        return "admin/employees";
    }

    @GetMapping("/web/admin/departments")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    public String departments(Model model, Authentication authentication) {
        addCommonAttributes(model, authentication, "departments", "부서 관리");
        return "admin/departments";
    }

    @GetMapping("/web/admin/roles")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'EMPLOYEE_ROLE')")
    public String roles(Model model, Authentication authentication) {
        addCommonAttributes(model, authentication, "roles", "역할·메뉴 권한");
        return "admin/roles";
    }

    private void addCommonAttributes(
            Model model,
            Authentication authentication,
            String adminTab,
            String pageTitle
    ) {
        model.addAttribute("activeMenu", "EMPLOYEE_ROLE");
        model.addAttribute("adminTab", adminTab);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("canWriteAdmin",
                menuPermissionEvaluator.hasWrite(authentication, "EMPLOYEE_ROLE"));
    }
}
