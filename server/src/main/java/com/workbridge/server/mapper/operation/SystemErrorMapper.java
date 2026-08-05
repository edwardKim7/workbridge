package com.workbridge.server.mapper.operation;

import com.workbridge.server.domain.operation.SystemErrorRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SystemErrorMapper {

    int insert(@Param("record")
    SystemErrorRecord r);

    List<SystemErrorRecord> selectPage(@Param("traceId")
    String traceId, @Param("httpStatus")
    Integer httpStatus, @Param("offset")
    int offset, @Param("size")
    int size);

    long countPage(@Param("traceId")
    String traceId, @Param("httpStatus")
    Integer httpStatus);

    List<SystemErrorRecord> selectRecent(@Param("limit")
    int limit);
}
