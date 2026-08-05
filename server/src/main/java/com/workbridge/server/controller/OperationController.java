package com.workbridge.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/admin/operations")
@PreAuthorize("hasRole('ADMIN')")
public class OperationController {

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'SYSTEM_OPERATION')")
    public String page(Model m) {
        m.addAttribute("activeMenu", "SYSTEM_OPERATION");
        return "admin/operations";
    }
}
