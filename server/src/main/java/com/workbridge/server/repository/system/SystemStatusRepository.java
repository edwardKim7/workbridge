package com.workbridge.server.repository.system;

import com.workbridge.server.mapper.system.SystemStatusMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SystemStatusRepository {

    private final SystemStatusMapper systemStatusMapper;

    public SystemStatusRepository(SystemStatusMapper systemStatusMapper) {
        this.systemStatusMapper = systemStatusMapper;
    }

    public boolean isDatabaseAvailable() {
        return systemStatusMapper.selectOne() == 1;
    }
}
