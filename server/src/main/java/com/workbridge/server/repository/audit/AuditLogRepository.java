package com.workbridge.server.repository.audit;

import com.workbridge.server.domain.operation.AuditLogRecord;
import com.workbridge.server.mapper.audit.AuditLogMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class AuditLogRepository {
    private final AuditLogMapper mapper;

    public AuditLogRepository(AuditLogMapper m) {
        mapper = m;
    }

    public void insert(Long actorId, String action, String targetType, String targetId, String beforeData, String afterData, String clientIp, String userAgent, String traceId) {
        mapper.insert(actorId, action, targetType, targetId, beforeData, afterData, clientIp, userAgent, traceId);
    }

    public List<AuditLogRecord> page(String a, String t, int o, int s) {
        return mapper.selectPage(a, t, o, s);
    }

    public long count(String a, String t) {
        return mapper.countPage(a, t);
    }

    public List<AuditLogRecord> recent(int n) {
        return mapper.selectRecent(n);
    }

    public List<AuditLogRecord> loginFailures(int n) {
        return mapper.selectRecentLoginFailures(n);
    }
}
