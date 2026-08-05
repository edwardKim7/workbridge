package com.workbridge.server.dto.dashboard;

import com.workbridge.server.domain.organization.OrganizationDashboardStats;

public class OrganizationDashboardResponse {

    private final long totalDepartments;
    private final long activeDepartments;
    private final long totalEmployees;
    private final long activeEmployees;
    private final long lockedEmployees;
    private final long activeRoles;

    public OrganizationDashboardResponse(OrganizationDashboardStats stats) {
        this.totalDepartments = stats.getTotalDepartments();
        this.activeDepartments = stats.getActiveDepartments();
        this.totalEmployees = stats.getTotalEmployees();
        this.activeEmployees = stats.getActiveEmployees();
        this.lockedEmployees = stats.getLockedEmployees();
        this.activeRoles = stats.getActiveRoles();
    }

    public long getTotalDepartments() { return totalDepartments; }
    public long getActiveDepartments() { return activeDepartments; }
    public long getTotalEmployees() { return totalEmployees; }
    public long getActiveEmployees() { return activeEmployees; }
    public long getLockedEmployees() { return lockedEmployees; }
    public long getActiveRoles() { return activeRoles; }
}
