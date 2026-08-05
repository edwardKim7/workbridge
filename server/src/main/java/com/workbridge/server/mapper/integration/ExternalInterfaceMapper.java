package com.workbridge.server.mapper.integration;

import com.workbridge.server.domain.integration.*;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ExternalInterfaceMapper {

    int insert(@Param("record")
    ExternalInterfaceRecord r);

    ExternalInterfaceRecord selectById(@Param("id")
    Long id);

    ExternalInterfaceRecord selectByIdempotencyKey(@Param("key")
    String key);

    List<ExternalInterfaceRecord> selectPage(@Param("status")
    InterfaceStatus status, @Param("traceId")
    String traceId, @Param("offset")
    int offset, @Param("size")
    int size);

    long countPage(@Param("status")
    InterfaceStatus status, @Param("traceId")
    String traceId);

    int markProcessing(@Param("id")
    Long id, @Param("status")
    InterfaceStatus status, @Param("traceId")
    String traceId, @Param("now")
    LocalDateTime now);

    int markResult(@Param("id")
    Long id, @Param("status")
    InterfaceStatus status, @Param("responseStatus")
    Integer responseStatus, @Param("responseBody")
    String responseBody, @Param("responseTruncated")
    boolean responseTruncated, @Param("errorCode")
    String errorCode, @Param("errorMessage")
    String errorMessage, @Param("retryCount")
    int retryCount, @Param("now")
    LocalDateTime now);

    int insertAttempt(@Param("record")
    ExternalInterfaceAttemptRecord r);

    List<ExternalInterfaceAttemptRecord> selectAttempts(@Param("id")
    Long id);

    List<ExternalInterfaceRecord> selectRecent(@Param("limit")
    int limit);

    long countRetryWaiting();
}
