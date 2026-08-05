package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EmployeeUpdateRequest {

    @NotBlank(message = "직원명은 필수입니다.")
    @Size(max = 100, message = "직원명은 100자 이하여야 합니다.")
    private String employeeName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식을 확인해 주세요.")
    @Size(max = 200, message = "이메일은 200자 이하여야 합니다.")
    private String email;

    @NotNull(message = "부서를 선택해 주세요.")
    private Long departmentId;

    @Min(value = 0, message = "버전은 0 이상이어야 합니다.")
    private long version;

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
