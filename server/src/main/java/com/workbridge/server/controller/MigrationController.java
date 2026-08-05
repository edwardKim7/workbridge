package com.workbridge.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/admin/migrations")
@PreAuthorize("hasRole('ADMIN')")
public class MigrationController {

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'DATA_MIGRATION')")
    public String page(Model m) {
        m.addAttribute("activeMenu", "DATA_MIGRATION");
        return "admin/migrations";
    }
}
