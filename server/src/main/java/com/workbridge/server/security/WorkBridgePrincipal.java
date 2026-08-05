package com.workbridge.server.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class WorkBridgePrincipal implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long employeeId;
    private final String employeeNo;
    private final String loginId;
    private final String employeeName;
    private final String email;
    private final Long departmentId;
    private final String departmentName;
    private final List<String> roleCodes;
    private final List<GrantedAuthority> authorities;

    public WorkBridgePrincipal(
            Long employeeId,
            String employeeNo,
            String loginId,
            String employeeName,
            String email,
            Long departmentId,
            String departmentName,
            List<String> roleCodes
    ) {
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
        this.employeeNo = Objects.requireNonNull(employeeNo, "employeeNo");
        this.loginId = Objects.requireNonNull(loginId, "loginId");
        this.employeeName = Objects.requireNonNull(employeeName, "employeeName");
        this.email = Objects.requireNonNull(email, "email");
        this.departmentId = Objects.requireNonNull(departmentId, "departmentId");
        this.departmentName = Objects.requireNonNull(departmentName, "departmentName");
        this.roleCodes = List.copyOf(roleCodes);
        this.authorities = this.roleCodes.stream()
                .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmail() {
        return email;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public boolean hasRole(String roleCode) {
        return roleCodes.contains(roleCode);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
