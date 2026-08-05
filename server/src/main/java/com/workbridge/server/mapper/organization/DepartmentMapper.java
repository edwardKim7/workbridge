package com.workbridge.server.mapper.organization;

import com.workbridge.server.domain.organization.DepartmentRecord;
import com.workbridge.server.domain.organization.DepartmentUpdateCommand;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DepartmentMapper {

    List<DepartmentRecord> selectPage(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countPage(@Param("keyword") String keyword, @Param("active") Boolean active);

    DepartmentRecord selectById(@Param("departmentId") Long departmentId);

    List<DepartmentRecord> selectAll();

    int countByCode(@Param("departmentCode") String departmentCode);

    int countActiveEmployees(@Param("departmentId") Long departmentId);

    int countActiveChildren(@Param("departmentId") Long departmentId);

    int insert(@Param("department") DepartmentRecord department, @Param("actorId") Long actorId);

    int update(DepartmentUpdateCommand command);
}
