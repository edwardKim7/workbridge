package com.workbridge.server.dto.auth;

import com.workbridge.server.dto.organization.RoleSummaryResponse;
import com.workbridge.server.dto.web.NavigationMenuResponse;

import java.util.List;

public class CurrentUserResponse {

    private final Long employeeId;
    private final String employeeNo;
    private final String loginId;
    private final String employeeName;
    private final String email;
    private final Long departmentId;
    private final String departmentName;
    private final List<RoleSummaryResponse> roles;
    private final List<NavigationMenuResponse> menus;

    public CurrentUserResponse(
            Long employeeId,
            String employeeNo,
            String loginId,
            String employeeName,
            String email,
            Long departmentId,
            String departmentName,
            List<RoleSummaryResponse> roles,
            List<NavigationMenuResponse> menus
    ) {
        this.employeeId = employeeId;
        this.employeeNo = employeeNo;
        this.loginId = loginId;
        this.employeeName = employeeName;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.roles = List.copyOf(roles);
        this.menus = List.copyOf(menus);
    }

    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public String getLoginId() { return loginId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmail() { return email; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public List<RoleSummaryResponse> getRoles() { return roles; }
    public List<NavigationMenuResponse> getMenus() { return menus; }
}
