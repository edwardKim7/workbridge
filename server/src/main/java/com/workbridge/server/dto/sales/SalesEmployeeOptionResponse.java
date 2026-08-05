package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.SalesEmployeeOptionRecord;

public class SalesEmployeeOptionResponse {

    private final Long employeeId;
    private final String employeeNo;
    private final String employeeName;
    private final String departmentName;

    public SalesEmployeeOptionResponse(SalesEmployeeOptionRecord employee) {
        this.employeeId = employee.getEmployeeId();
        this.employeeNo = employee.getEmployeeNo();
        this.employeeName = employee.getEmployeeName();
        this.departmentName = employee.getDepartmentName();
    }

    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeNo() { return employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public String getDepartmentName() { return departmentName; }
}
