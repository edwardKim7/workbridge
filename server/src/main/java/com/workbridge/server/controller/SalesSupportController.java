package com.workbridge.server.controller;

import com.workbridge.server.security.MenuPermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SalesSupportController {

    private final MenuPermissionEvaluator menuPermissionEvaluator;

    public SalesSupportController(MenuPermissionEvaluator menuPermissionEvaluator) {
        this.menuPermissionEvaluator = menuPermissionEvaluator;
    }

    @GetMapping("/web/customers")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'CUSTOMER')")
    public String customers(Model model, Authentication authentication) {
        addCustomerPermissions(model, authentication);
        model.addAttribute("activeMenu", "CUSTOMER");
        model.addAttribute("pageTitle", "고객 관리");
        return "sales/customers";
    }

    @GetMapping("/web/customers/{customerId}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'CUSTOMER')")
    public String customerDetail(
            @PathVariable Long customerId,
            Model model,
            Authentication authentication
    ) {
        addCustomerPermissions(model, authentication);
        model.addAttribute("canReadOpportunity",
                menuPermissionEvaluator.hasRead(authentication, "OPPORTUNITY"));
        model.addAttribute("canWriteOpportunity",
                menuPermissionEvaluator.hasWrite(authentication, "OPPORTUNITY"));
        model.addAttribute("canReadActivity",
                menuPermissionEvaluator.hasRead(authentication, "SALES_ACTIVITY"));
        model.addAttribute("canWriteActivity",
                menuPermissionEvaluator.hasWrite(authentication, "SALES_ACTIVITY"));
        model.addAttribute("activeMenu", "CUSTOMER");
        model.addAttribute("pageTitle", "고객 상세");
        model.addAttribute("customerId", customerId);
        return "sales/customer-detail";
    }

    @GetMapping("/web/opportunities")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'OPPORTUNITY')")
    public String opportunities(Model model, Authentication authentication) {
        addOpportunityPermissions(model, authentication);
        model.addAttribute("canReadCustomer",
                menuPermissionEvaluator.hasRead(authentication, "CUSTOMER"));
        model.addAttribute("canReadActivity",
                menuPermissionEvaluator.hasRead(authentication, "SALES_ACTIVITY"));
        model.addAttribute("activeMenu", "OPPORTUNITY");
        model.addAttribute("pageTitle", "영업기회 관리");
        return "sales/opportunities";
    }

    @GetMapping("/web/opportunities/{opportunityId}")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'OPPORTUNITY')")
    public String opportunityDetail(
            @PathVariable Long opportunityId,
            Model model,
            Authentication authentication
    ) {
        addOpportunityPermissions(model, authentication);
        model.addAttribute("canReadCustomer",
                menuPermissionEvaluator.hasRead(authentication, "CUSTOMER"));
        model.addAttribute("canReadActivity",
                menuPermissionEvaluator.hasRead(authentication, "SALES_ACTIVITY"));
        model.addAttribute("activeMenu", "OPPORTUNITY");
        model.addAttribute("pageTitle", "영업기회 상세");
        model.addAttribute("opportunityId", opportunityId);
        return "sales/opportunity-detail";
    }

    @GetMapping("/web/activities")
    @PreAuthorize("@menuPermission.hasRead(authentication, 'SALES_ACTIVITY')")
    public String activities(Model model, Authentication authentication) {
        boolean canWrite = menuPermissionEvaluator.hasWrite(authentication, "SALES_ACTIVITY");
        model.addAttribute("activeMenu", "SALES_ACTIVITY");
        model.addAttribute("pageTitle", "영업활동");
        model.addAttribute("canWriteSales", canWrite);
        model.addAttribute("canWriteActivity", canWrite);
        return "sales/activities";
    }

    private void addCustomerPermissions(Model model, Authentication authentication) {
        boolean canWrite = menuPermissionEvaluator.hasWrite(authentication, "CUSTOMER");
        model.addAttribute("canWriteCustomer", canWrite);
        model.addAttribute("canWriteSales", canWrite);
    }

    private void addOpportunityPermissions(Model model, Authentication authentication) {
        boolean canWriteOpportunity = menuPermissionEvaluator.hasWrite(authentication, "OPPORTUNITY");
        boolean canWriteActivity = menuPermissionEvaluator.hasWrite(authentication, "SALES_ACTIVITY");
        model.addAttribute("canWriteOpportunity", canWriteOpportunity);
        model.addAttribute("canWriteSales", canWriteOpportunity);
        model.addAttribute("canWriteActivity", canWriteActivity);
    }
}
