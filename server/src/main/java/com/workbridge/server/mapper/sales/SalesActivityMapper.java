package com.workbridge.server.mapper.sales;

import com.workbridge.server.domain.sales.SalesActivityRecord;
import com.workbridge.server.domain.sales.SalesActivityType;
import com.workbridge.server.domain.sales.SalesActivityUpdateCommand;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesActivityMapper {

    List<SalesActivityRecord> selectPage(
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("opportunityId") Long opportunityId,
            @Param("employeeId") Long employeeId,
            @Param("activityType") SalesActivityType activityType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countPage(
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("opportunityId") Long opportunityId,
            @Param("employeeId") Long employeeId,
            @Param("activityType") SalesActivityType activityType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    SalesActivityRecord selectById(@Param("activityId") Long activityId);

    int insert(@Param("activity") SalesActivityRecord activity, @Param("actorId") Long actorId);

    int update(SalesActivityUpdateCommand command);
}
