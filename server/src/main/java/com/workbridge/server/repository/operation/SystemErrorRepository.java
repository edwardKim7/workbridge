package com.workbridge.server.repository.operation;

import com.workbridge.server.domain.operation.SystemErrorRecord;
import com.workbridge.server.mapper.operation.SystemErrorMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SystemErrorRepository {
    private final SystemErrorMapper m;

    public SystemErrorRepository(SystemErrorMapper m) {
        this.m = m;
    }

    public void insert(SystemErrorRecord r) {
        m.insert(r);
    }

    public List<SystemErrorRecord> page(String t, Integer h, int o, int s) {
        return m.selectPage(t, h, o, s);
    }

    public long count(String t, Integer h) {
        return m.countPage(t, h);
    }

    public List<SystemErrorRecord> recent(int n) {
        return m.selectRecent(n);
    }
}
