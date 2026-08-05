package com.workbridge.server.repository.sales;

import com.workbridge.server.domain.sales.SalesActivityRecord;
import com.workbridge.server.domain.sales.SalesActivityType;
import com.workbridge.server.domain.sales.SalesActivityUpdateCommand;
import com.workbridge.server.mapper.sales.SalesActivityMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SalesActivityRepository {

    private final SalesActivityMapper mapper;

    public SalesActivityRepository(SalesActivityMapper mapper) {
        this.mapper = mapper;
    }

    public List<SalesActivityRecord> findPage(
            String keyword,
            Long customerId,
            Long opportunityId,
            Long employeeId,
            SalesActivityType activityType,
            LocalDateTime from,
            LocalDateTime to,
            int offset,
            int size
    ) {
        return mapper.selectPage(keyword, customerId, opportunityId, employeeId, activityType,
                from, to, offset, size);
    }

    public long count(
            String keyword,
            Long customerId,
            Long opportunityId,
            Long employeeId,
            SalesActivityType activityType,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return mapper.countPage(keyword, customerId, opportunityId, employeeId, activityType, from, to);
    }

    public Optional<SalesActivityRecord> findById(Long activityId) {
        return Optional.ofNullable(mapper.selectById(activityId));
    }

    public Long insert(SalesActivityRecord activity, Long actorId) {
        mapper.insert(activity, actorId);
        return activity.getActivityId();
    }

    public boolean update(SalesActivityUpdateCommand command) {
        return mapper.update(command) == 1;
    }
}
