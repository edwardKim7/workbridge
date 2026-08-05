package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DepartmentCreateRequest {

    @NotBlank(message = "부서코드는 필수입니다.")
    @Pattern(regexp = "^[A-Z0-9_]{2,30}$", message = "부서코드는 영문 대문자, 숫자, 밑줄 2~30자로 입력해 주세요.")
    private String departmentCode;

    @NotBlank(message = "부서명은 필수입니다.")
    @Size(max = 100, message = "부서명은 100자 이하여야 합니다.")
    private String departmentName;

    private Long parentDepartmentId;
    private boolean active = true;

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getParentDepartmentId() { return parentDepartmentId; }
    public void setParentDepartmentId(Long parentDepartmentId) { this.parentDepartmentId = parentDepartmentId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
