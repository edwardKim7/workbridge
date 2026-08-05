package com.workbridge.server.service.integration;

import com.workbridge.server.domain.integration.*;
import com.workbridge.server.repository.integration.ExternalInterfaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.LocalDateTime;

@Service
public class InterfaceTransactionService {
    private final ExternalInterfaceRepository repository;

    public InterfaceTransactionService(ExternalInterfaceRepository r) {
        repository = r;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long create(ExternalInterfaceRecord r) {
        return repository.insert(r);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processing(Long id, InterfaceStatus status, String trace, LocalDateTime now) {
        return repository.markProcessing(id, status, trace, now);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(Long id, int http, String body, boolean truncated, int retry, LocalDateTime start, LocalDateTime end, String trace) {
        repository.markResult(id, InterfaceStatus.SUCCESS, http, body, truncated, null, null, retry, end);
        ExternalInterfaceAttemptRecord a = attempt(id, retry + 1, InterfaceStatus.SUCCESS, http, body, null, null, trace, start, end);
        repository.attempt(a);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failure(Long id, InterfaceStatus status, Integer http, String body, boolean truncated, String code, String message, int retry, LocalDateTime start, LocalDateTime end, String trace) {
        repository.markResult(id, status, http, body, truncated, code, message, retry, end);
        repository.attempt(attempt(id, retry + 1, status, http, body, code, message, trace, start, end));
    }

    private ExternalInterfaceAttemptRecord attempt(Long id, int no, InterfaceStatus s, Integer http, String body, String code, String msg, String trace, LocalDateTime start, LocalDateTime end) {
        ExternalInterfaceAttemptRecord a = new ExternalInterfaceAttemptRecord();
        a.setInterfaceId(id);
        a.setAttemptNo(no);
        a.setStatus(s);
        a.setResponseStatus(http);
        a.setResponseBody(body);
        a.setErrorCode(code);
        a.setErrorMessage(msg);
        a.setTraceId(trace);
        a.setStartedAt(start);
        a.setCompletedAt(end);
        return a;
    }
}
