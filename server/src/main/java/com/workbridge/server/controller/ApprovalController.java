package com.workbridge.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/approvals")
public class ApprovalController {

    @GetMapping
    @PreAuthorize("@menuPermission.hasRead(authentication, 'APPROVAL')")
    public String list(Model model) {
        model.addAttribute("activeMenu", "APPROVAL");
        return "approval/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'APPROVAL')")
    public String detail(@PathVariable
    Long id, Model model) {
        model.addAttribute("activeMenu", "APPROVAL");
        model.addAttribute("approvalId", id);
        return "approval/detail";
    }
}
