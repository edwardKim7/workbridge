package com.workbridge.server.domain.organization;

public class DepartmentUpdateCommand {

    private final Long departmentId;
    private final String departmentName;
    private final Long parentDepartmentId;
    private final boolean active;
    private final Long actorId;
    private final long version;

    public DepartmentUpdateCommand(
            Long departmentId,
            String departmentName,
            Long parentDepartmentId,
            boolean active,
            Long actorId,
            long version
    ) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.parentDepartmentId = parentDepartmentId;
        this.active = active;
        this.actorId = actorId;
        this.version = version;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Long getParentDepartmentId() {
        return parentDepartmentId;
    }

    public boolean isActive() {
        return active;
    }

    public Long getActorId() {
        return actorId;
    }

    public long getVersion() {
        return version;
    }

}
