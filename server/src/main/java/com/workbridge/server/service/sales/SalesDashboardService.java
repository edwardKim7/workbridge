package com.workbridge.server.service.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStageCount;
import com.workbridge.server.domain.sales.SalesDashboardSummary;
import com.workbridge.server.dto.sales.SalesDashboardResponse;
import com.workbridge.server.repository.sales.SalesDashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesDashboardService {

    private final SalesDashboardRepository dashboardRepository;
    private final SalesAccessPolicy salesAccessPolicy;
    private final Clock clock;

    public SalesDashboardService(
            SalesDashboardRepository dashboardRepository,
            SalesAccessPolicy salesAccessPolicy,
            Clock clock
    ) {
        this.dashboardRepository = dashboardRepository;
        this.salesAccessPolicy = salesAccessPolicy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SalesDashboardResponse getSummary() {
        Long ownerEmployeeId = salesAccessPolicy.dashboardOwnerFilter();
        LocalDate monthStart = LocalDate.now(clock).withDayOfMonth(1);
        SalesDashboardSummary summary = dashboardRepository.getSummary(
                ownerEmployeeId, monthStart, monthStart.plusMonths(1));

        Map<OpportunityStage, Long> counts = new EnumMap<>(OpportunityStage.class);
        dashboardRepository.getStageCounts(ownerEmployeeId)
                .forEach(value -> counts.put(value.getStage(), value.getCount()));
        List<OpportunityStageCount> normalizedCounts = Arrays.stream(OpportunityStage.values())
                .map(stage -> new OpportunityStageCount(stage, counts.getOrDefault(stage, 0L)))
                .toList();
        return new SalesDashboardResponse(
                ownerEmployeeId == null ? "ALL" : "MY",
                summary,
                normalizedCounts
        );
    }
}
