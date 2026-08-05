package com.workbridge.server.mapper.audit;

import com.workbridge.server.domain.operation.AuditLogRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AuditLogMapper {

    int insert(@Param("actorId")
    Long actorId, @Param("action")
    String action, @Param("targetType")
    String targetType, @Param("targetId")
    String targetId, @Param("beforeData")
    String beforeData, @Param("afterData")
    String afterData, @Param("clientIp")
    String clientIp, @Param("userAgent")
    String userAgent, @Param("traceId")
    String traceId);

    List<AuditLogRecord> selectPage(@Param("action")
    String action, @Param("traceId")
    String traceId, @Param("offset")
    int offset, @Param("size")
    int size);

    long countPage(@Param("action")
    String action, @Param("traceId")
    String traceId);

    List<AuditLogRecord> selectRecent(@Param("limit")
    int limit);

    List<AuditLogRecord> selectRecentLoginFailures(@Param("limit")
    int limit);
}
