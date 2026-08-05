package com.workbridge.server.repository.sales;

import com.workbridge.server.domain.sales.OpportunityStageCount;
import com.workbridge.server.domain.sales.SalesDashboardSummary;
import com.workbridge.server.mapper.sales.SalesDashboardMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class SalesDashboardRepository {

    private final SalesDashboardMapper mapper;

    public SalesDashboardRepository(SalesDashboardMapper mapper) {
        this.mapper = mapper;
    }

    public SalesDashboardSummary getSummary(
            Long ownerEmployeeId,
            LocalDate monthStart,
            LocalDate nextMonthStart
    ) {
        return mapper.selectSummary(ownerEmployeeId, monthStart, nextMonthStart);
    }

    public List<OpportunityStageCount> getStageCounts(Long ownerEmployeeId) {
        return mapper.selectStageCounts(ownerEmployeeId);
    }
}
