package com.workbridge.server.domain.organization;

public class EmployeeStateCommand {

    private final Long employeeId;
    private final Long actorId;
    private final long version;

    public EmployeeStateCommand(
            Long employeeId,
            Long actorId,
            long version
    ) {
        this.employeeId = employeeId;
        this.actorId = actorId;
        this.version = version;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getActorId() {
        return actorId;
    }

    public long getVersion() {
        return version;
    }

}
