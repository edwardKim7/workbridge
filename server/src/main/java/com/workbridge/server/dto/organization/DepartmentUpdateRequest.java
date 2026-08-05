package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentUpdateRequest {

    @NotBlank(message = "부서명은 필수입니다.")
    @Size(max = 100, message = "부서명은 100자 이하여야 합니다.")
    private String departmentName;

    private Long parentDepartmentId;
    private boolean active;

    @Min(value = 0, message = "버전은 0 이상이어야 합니다.")
    private long version;

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getParentDepartmentId() { return parentDepartmentId; }
    public void setParentDepartmentId(Long parentDepartmentId) { this.parentDepartmentId = parentDepartmentId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
