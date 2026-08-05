package com.workbridge.server.service.migration;

import com.workbridge.server.domain.migration.*;
import com.workbridge.server.repository.migration.MigrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.*;
import java.util.*;

@Service
public class MigrationTransactionService {
    private final MigrationRepository r;
    private final Clock clock;

    public MigrationTransactionService(MigrationRepository r, Clock c) {
        this.r = r;
        clock = c;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long create(MigrationBatchRecord b) {
        return r.insertBatch(b);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void stage(Long id, List<MigrationStagingRecord> rows, List<MigrationErrorRecord> errors, int chunk) {
        r.status(id, MigrationBatchStatus.STAGING, rows.size(), 0, errors.stream().map(MigrationErrorRecord::getRowNo).distinct().toList().size(), null, false);
        for (int i = 0; i < rows.size(); i += chunk) r.staging(rows.subList(i, Math.min(rows.size(), i + chunk)));
        r.errors(errors);
        r.status(id, MigrationBatchStatus.VALIDATING, rows.size(), 0, errors.stream().map(MigrationErrorRecord::getRowNo).distinct().toList().size(), null, false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int apply(Long id, Long actor) {
        r.status(id, MigrationBatchStatus.APPLYING, 0, 0, 0, null, false);
        List<MigrationStagingRecord> rows = r.valid(id);
        for (MigrationStagingRecord row : rows) {
            if (row.getOperationType() == MigrationOperationType.UPDATE) r.updateCustomer(row, actor); else if (row.getOperationType() == MigrationOperationType.INSERT) r.insertCustomer(row, actor);
        }
        return rows.size();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(Long id, int total, int success, int failure) {
        r.status(id, failure == 0 ? MigrationBatchStatus.COMPLETED : MigrationBatchStatus.COMPLETED_WITH_ERRORS, total, success, failure, LocalDateTime.now(clock), false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long id, int total, int failure) {
        MigrationBatchRecord b = r.find(id).orElse(null);
        if (b != null && (b.getStatus() == MigrationBatchStatus.COMPLETED || b.getStatus() == MigrationBatchStatus.COMPLETED_WITH_ERRORS)) return;
        r.status(id, MigrationBatchStatus.FAILED, total, 0, failure, LocalDateTime.now(clock), true);
    }
}
