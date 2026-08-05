package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.Min;

public class EmployeeStateChangeRequest {

    @Min(value = 0, message = "버전은 0 이상이어야 합니다.")
    private long version;

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
