package com.workbridge.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/admin/interfaces")
@PreAuthorize("hasRole('ADMIN')")
public class InterfaceOperationController {

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'INTERFACE_OPERATION')")
    public String list(Model m) {
        m.addAttribute("activeMenu", "INTERFACE_OPERATION");
        return "admin/interfaces";
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'INTERFACE_OPERATION')")
    public String detail(@PathVariable
    Long id, Model m) {
        m.addAttribute("activeMenu", "INTERFACE_OPERATION");
        m.addAttribute("interfaceId", id);
        return "admin/interface-detail";
    }
}
