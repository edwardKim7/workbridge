package com.workbridge.server.repository.integration;

import com.workbridge.server.domain.integration.*;
import com.workbridge.server.mapper.integration.ExternalInterfaceMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class ExternalInterfaceRepository {
    private final ExternalInterfaceMapper mapper;

    public ExternalInterfaceRepository(ExternalInterfaceMapper mapper) {
        this.mapper = mapper;
    }

    public Long insert(ExternalInterfaceRecord r) {
        mapper.insert(r);
        return r.getInterfaceId();
    }

    public Optional<ExternalInterfaceRecord> find(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<ExternalInterfaceRecord> findByKey(String key) {
        return Optional.ofNullable(mapper.selectByIdempotencyKey(key));
    }

    public List<ExternalInterfaceRecord> page(InterfaceStatus s, String trace, int off, int size) {
        return mapper.selectPage(s, trace, off, size);
    }

    public long count(InterfaceStatus s, String trace) {
        return mapper.countPage(s, trace);
    }

    public boolean markProcessing(Long id, InterfaceStatus s, String trace, LocalDateTime now) {
        return mapper.markProcessing(id, s, trace, now) == 1;
    }

    public void markResult(Long id, InterfaceStatus s, Integer status, String body, boolean trunc, String code, String message, int retries, LocalDateTime now) {
        mapper.markResult(id, s, status, body, trunc, code, message, retries, now);
    }

    public void attempt(ExternalInterfaceAttemptRecord r) {
        mapper.insertAttempt(r);
    }

    public List<ExternalInterfaceAttemptRecord> attempts(Long id) {
        return mapper.selectAttempts(id);
    }

    public List<ExternalInterfaceRecord> recent(int n) {
        return mapper.selectRecent(n);
    }

    public long retryWaiting() {
        return mapper.countRetryWaiting();
    }
}
