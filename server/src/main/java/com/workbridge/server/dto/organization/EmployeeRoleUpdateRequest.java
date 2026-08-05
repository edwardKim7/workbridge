package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EmployeeRoleUpdateRequest {

    @NotEmpty(message = "역할을 하나 이상 선택해 주세요.")
    private List<@NotNull(message = "역할 ID는 null일 수 없습니다.") Long> roleIds = new ArrayList<>();

    @Min(value = 0, message = "버전은 0 이상이어야 합니다.")
    private long version;

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
