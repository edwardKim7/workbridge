package com.workbridge.server.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class EmployeeCreateRequest {

    @NotBlank(message = "사번은 필수입니다.")
    @Pattern(regexp = "^[A-Z0-9-]{3,30}$", message = "사번은 영문 대문자, 숫자, 하이픈 3~30자로 입력해 주세요.")
    private String employeeNo;

    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Pattern(regexp = "^[a-z0-9._-]{4,50}$", message = "로그인 ID는 영문 소문자, 숫자, 점, 밑줄, 하이픈 4~50자로 입력해 주세요.")
    private String loginId;

    @NotBlank(message = "초기 비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,72}$",
            message = "비밀번호는 영문 대·소문자, 숫자, 특수문자를 포함한 10~72자로 입력해 주세요."
    )
    private String password;

    @NotBlank(message = "직원명은 필수입니다.")
    @Size(max = 100, message = "직원명은 100자 이하여야 합니다.")
    private String employeeName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식을 확인해 주세요.")
    @Size(max = 200, message = "이메일은 200자 이하여야 합니다.")
    private String email;

    @NotNull(message = "부서를 선택해 주세요.")
    private Long departmentId;

    private boolean active = true;

    @NotEmpty(message = "역할을 하나 이상 선택해 주세요.")
    private List<@NotNull(message = "역할 ID는 null일 수 없습니다.") Long> roleIds = new ArrayList<>();

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
