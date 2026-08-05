package com.workbridge.server.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.audit.*;
import com.workbridge.server.repository.audit.AuditLogRepository;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;
    private final AuditRequestMetadataProvider metadataProvider;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository repository, AuditRequestMetadataProvider metadataProvider, ObjectMapper objectMapper) {
        this.repository = repository;
        this.metadataProvider = metadataProvider;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(Long actorId, String action, String targetType, String targetId, Object beforeData, Object afterData) {
        doRecord(actorId, action, targetType, targetId, beforeData, afterData);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIndependent(Long actorId, String action, String targetType, String targetId, Object beforeData, Object afterData) {
        doRecord(actorId, action, targetType, targetId, beforeData, afterData);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void safeRecordIndependent(Long actorId, String action, String targetType, String targetId, Object beforeData, Object afterData) {
        try {
            doRecord(actorId, action, targetType, targetId, beforeData, afterData);
        } catch (RuntimeException e) {
            log.warn("Audit log write failed. action={}, targetType={}, targetId={}", action, targetType, targetId);
        }
    }

    private void doRecord(Long actorId, String action, String targetType, String targetId, Object beforeData, Object afterData) {
        AuditRequestMetadata m = metadataProvider.current();
        repository.insert(actorId, truncate(action, 60), truncate(targetType, 60), truncate(targetId, 100), toJson(beforeData), toJson(afterData), m.getClientIp(), m.getUserAgent(), m.getTraceId());
    }

    private String toJson(Object v) {
        if (v == null) return null;
        try {
            return objectMapper.writeValueAsString(v);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":true}";
        }
    }

    private String truncate(String v, int n) {
        return v == null ? null : (v.length() <= n ? v : v.substring(0, n));
    }
}
