package com.workbridge.server.controller;

import com.workbridge.server.security.WorkBridgePrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof WorkBridgePrincipal) {
            return "redirect:/web/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof WorkBridgePrincipal) {
            return "redirect:/web/dashboard";
        }
        model.addAttribute("pageTitle", "로그인");
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "접근 권한 없음");
        return "access-denied";
    }
}
