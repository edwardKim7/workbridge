package com.workbridge.server.mapper.sales;

import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;
import com.workbridge.server.domain.sales.SalesOpportunityUpdateCommand;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalesOpportunityMapper {

    List<SalesOpportunityRecord> selectPage(
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("stage") OpportunityStage stage,
            @Param("status") OpportunityStatus status,
            @Param("ownerEmployeeId") Long ownerEmployeeId,
            @Param("closeDateFrom") LocalDate closeDateFrom,
            @Param("closeDateTo") LocalDate closeDateTo,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countPage(
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("stage") OpportunityStage stage,
            @Param("status") OpportunityStatus status,
            @Param("ownerEmployeeId") Long ownerEmployeeId,
            @Param("closeDateFrom") LocalDate closeDateFrom,
            @Param("closeDateTo") LocalDate closeDateTo
    );

    SalesOpportunityRecord selectById(@Param("opportunityId") Long opportunityId);

    List<SalesOpportunityRecord> selectOptionsByCustomer(
            @Param("customerId") Long customerId,
            @Param("limit") int limit
    );

    int insert(@Param("opportunity") SalesOpportunityRecord opportunity, @Param("actorId") Long actorId);

    int update(SalesOpportunityUpdateCommand command);
}
