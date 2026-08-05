package com.workbridge.server.mapper.sales;

import com.workbridge.server.domain.sales.OpportunityStageCount;
import com.workbridge.server.domain.sales.SalesDashboardSummary;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalesDashboardMapper {

    SalesDashboardSummary selectSummary(
            @Param("ownerEmployeeId") Long ownerEmployeeId,
            @Param("monthStart") LocalDate monthStart,
            @Param("nextMonthStart") LocalDate nextMonthStart
    );

    List<OpportunityStageCount> selectStageCounts(@Param("ownerEmployeeId") Long ownerEmployeeId);
}
