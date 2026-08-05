package com.workbridge.server.repository.migration;

import com.workbridge.server.domain.migration.*;
import com.workbridge.server.mapper.migration.MigrationMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class MigrationRepository {
    private final MigrationMapper m;

    public MigrationRepository(MigrationMapper m) {
        this.m = m;
    }

    public Long insertBatch(MigrationBatchRecord b) {
        m.insertBatch(b);
        return b.getBatchId();
    }

    public Optional<MigrationBatchRecord> find(Long id) {
        return Optional.ofNullable(m.selectBatch(id));
    }

    public Optional<MigrationBatchRecord> completedByHash(String h) {
        return Optional.ofNullable(m.selectCompletedByHash(h));
    }

    public List<MigrationBatchRecord> page(MigrationBatchStatus s, int o, int z) {
        return m.selectPage(s, o, z);
    }

    public long count(MigrationBatchStatus s) {
        return m.countPage(s);
    }

    public void status(Long id, MigrationBatchStatus s, int t, int ok, int fail, LocalDateTime end, boolean clear) {
        m.updateStatus(id, s, t, ok, fail, end, clear);
    }

    public void staging(List<MigrationStagingRecord> r) {
        if (!r.isEmpty()) m.insertStaging(r);
    }

    public void errors(List<MigrationErrorRecord> e) {
        if (!e.isEmpty()) m.insertErrors(e);
    }

    public List<MigrationStagingRecord> valid(Long id) {
        return m.selectValidRows(id);
    }

    public List<MigrationErrorRecord> errors(Long id) {
        return m.selectErrors(id);
    }

    public Long customer(String b) {
        return m.selectCustomerId(b);
    }

    public Long sales(String e) {
        return m.selectActiveSalesEmployeeId(e);
    }

    public void resolve(MigrationStagingRecord r) {
        m.updateStagingResolution(r.getStagingId(), r.getValidationStatus(), r.getOperationType(), r.getExistingCustomerId(), r.getAssignedSalesId());
    }

    public void updateCustomer(MigrationStagingRecord r, Long a) {
        m.updateCustomer(r, a);
    }

    public void insertCustomer(MigrationStagingRecord r, Long a) {
        m.insertCustomer(r, a);
    }

    public List<MigrationBatchRecord> recent(int n) {
        return m.selectRecent(n);
    }
}
