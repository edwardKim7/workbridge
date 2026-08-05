package com.workbridge.server.mapper.sales;

import com.workbridge.server.domain.sales.SalesEmployeeOptionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SalesEmployeeMapper {

    List<SalesEmployeeOptionRecord> selectActiveSalesEmployees();

    int countActiveSalesEmployee(@Param("employeeId") Long employeeId);
}
