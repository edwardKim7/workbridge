package com.workbridge.server.domain.organization;

public class OrganizationDashboardStats {

    private long totalDepartments;
    private long activeDepartments;
    private long totalEmployees;
    private long activeEmployees;
    private long lockedEmployees;
    private long activeRoles;

    public OrganizationDashboardStats() {
    }

    public OrganizationDashboardStats(
            long totalDepartments,
            long activeDepartments,
            long totalEmployees,
            long activeEmployees,
            long lockedEmployees,
            long activeRoles
    ) {
        this.totalDepartments = totalDepartments;
        this.activeDepartments = activeDepartments;
        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.lockedEmployees = lockedEmployees;
        this.activeRoles = activeRoles;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getActiveDepartments() {
        return activeDepartments;
    }

    public void setActiveDepartments(long activeDepartments) {
        this.activeDepartments = activeDepartments;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(long activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public long getLockedEmployees() {
        return lockedEmployees;
    }

    public void setLockedEmployees(long lockedEmployees) {
        this.lockedEmployees = lockedEmployees;
    }

    public long getActiveRoles() {
        return activeRoles;
    }

    public void setActiveRoles(long activeRoles) {
        this.activeRoles = activeRoles;
    }

}
