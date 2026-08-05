package com.workbridge.server.domain.organization;

public class EmployeeUpdateCommand {

    private final Long employeeId;
    private final String employeeName;
    private final String email;
    private final Long departmentId;
    private final Long actorId;
    private final long version;

    public EmployeeUpdateCommand(
            Long employeeId,
            String employeeName,
            String email,
            Long departmentId,
            Long actorId,
            long version
    ) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.email = email;
        this.departmentId = departmentId;
        this.actorId = actorId;
        this.version = version;
    }

    public Long getEmployeeId() {
        return employeeId;
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

    public Long getActorId() {
        return actorId;
    }

    public long getVersion() {
        return version;
    }

}
