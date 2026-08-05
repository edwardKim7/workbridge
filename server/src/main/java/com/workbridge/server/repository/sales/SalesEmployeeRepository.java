package com.workbridge.server.repository.sales;

import com.workbridge.server.domain.sales.SalesEmployeeOptionRecord;
import com.workbridge.server.mapper.sales.SalesEmployeeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SalesEmployeeRepository {

    private final SalesEmployeeMapper mapper;

    public SalesEmployeeRepository(SalesEmployeeMapper mapper) {
        this.mapper = mapper;
    }

    public List<SalesEmployeeOptionRecord> findActiveSalesEmployees() {
        return mapper.selectActiveSalesEmployees();
    }

    public boolean isActiveSalesEmployee(Long employeeId) {
        return employeeId != null && mapper.countActiveSalesEmployee(employeeId) > 0;
    }
}
