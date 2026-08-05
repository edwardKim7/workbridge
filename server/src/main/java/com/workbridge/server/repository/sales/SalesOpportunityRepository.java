package com.workbridge.server.repository.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;
import com.workbridge.server.domain.sales.SalesOpportunityUpdateCommand;
import com.workbridge.server.mapper.sales.SalesOpportunityMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class SalesOpportunityRepository {

    private final SalesOpportunityMapper mapper;

    public SalesOpportunityRepository(SalesOpportunityMapper mapper) {
        this.mapper = mapper;
    }

    public List<SalesOpportunityRecord> findPage(
            String keyword,
            Long customerId,
            OpportunityStage stage,
            OpportunityStatus status,
            Long ownerEmployeeId,
            LocalDate closeDateFrom,
            LocalDate closeDateTo,
            int offset,
            int size
    ) {
        return mapper.selectPage(keyword, customerId, stage, status, ownerEmployeeId,
                closeDateFrom, closeDateTo, offset, size);
    }

    public long count(
            String keyword,
            Long customerId,
            OpportunityStage stage,
            OpportunityStatus status,
            Long ownerEmployeeId,
            LocalDate closeDateFrom,
            LocalDate closeDateTo
    ) {
        return mapper.countPage(keyword, customerId, stage, status, ownerEmployeeId,
                closeDateFrom, closeDateTo);
    }

    public Optional<SalesOpportunityRecord> findById(Long opportunityId) {
        return Optional.ofNullable(mapper.selectById(opportunityId));
    }

    public List<SalesOpportunityRecord> findOptionsByCustomer(Long customerId, int limit) {
        return mapper.selectOptionsByCustomer(customerId, limit);
    }

    public Long insert(SalesOpportunityRecord opportunity, Long actorId) {
        mapper.insert(opportunity, actorId);
        return opportunity.getOpportunityId();
    }

    public boolean update(SalesOpportunityUpdateCommand command) {
        return mapper.update(command) == 1;
    }
}
