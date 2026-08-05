package com.workbridge.server.mapper.migration;

import com.workbridge.server.domain.migration.*;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MigrationMapper {

    int insertBatch(@Param("batch")
    MigrationBatchRecord b);

    MigrationBatchRecord selectBatch(@Param("id")
    Long id);

    MigrationBatchRecord selectCompletedByHash(@Param("hash")
    String hash);

    List<MigrationBatchRecord> selectPage(@Param("status")
    MigrationBatchStatus status, @Param("offset")
    int offset, @Param("size")
    int size);

    long countPage(@Param("status")
    MigrationBatchStatus status);

    int updateStatus(@Param("id")
    Long id, @Param("status")
    MigrationBatchStatus status, @Param("total")
    int total, @Param("success")
    int success, @Param("failure")
    int failure, @Param("completedAt")
    LocalDateTime completedAt, @Param("clearDedup")
    boolean clearDedup);

    int insertStaging(@Param("rows")
    List<MigrationStagingRecord> rows);

    int insertErrors(@Param("errors")
    List<MigrationErrorRecord> errors);

    List<MigrationStagingRecord> selectValidRows(@Param("id")
    Long id);

    List<MigrationErrorRecord> selectErrors(@Param("id")
    Long id);

    Long selectCustomerId(@Param("businessNo")
    String businessNo);

    Long selectActiveSalesEmployeeId(@Param("employeeNo")
    String employeeNo);

    int updateStagingResolution(@Param("stagingId")
    Long stagingId, @Param("status")
    MigrationValidationStatus status, @Param("operation")
    MigrationOperationType operation, @Param("existingId")
    Long existingId, @Param("assignedSalesId")
    Long assignedSalesId);

    int updateCustomer(@Param("row")
    MigrationStagingRecord row, @Param("actor")
    Long actor);

    int insertCustomer(@Param("row")
    MigrationStagingRecord row, @Param("actor")
    Long actor);

    List<MigrationBatchRecord> selectRecent(@Param("limit")
    int limit);
}
