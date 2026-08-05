package com.workbridge.server.service.operation;

import com.workbridge.server.domain.operation.SystemErrorRecord;
import com.workbridge.server.repository.operation.SystemErrorRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemErrorTransactionWriter {

    private final SystemErrorRepository repository;

    public SystemErrorTransactionWriter(SystemErrorRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(SystemErrorRecord record) {
        repository.insert(record);
    }
}
