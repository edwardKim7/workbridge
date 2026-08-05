package com.workbridge.server.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.audit.TraceIdContext;
import com.workbridge.server.config.ErpIntegrationProperties;
import com.workbridge.server.domain.approval.*;
import com.workbridge.server.domain.integration.*;
import com.workbridge.server.domain.sales.*;
import com.workbridge.server.dto.integration.*;
import com.workbridge.server.exception.*;
import com.workbridge.server.integration.*;
import com.workbridge.server.repository.approval.ApprovalRepository;
import com.workbridge.server.repository.integration.ExternalInterfaceRepository;
import com.workbridge.server.repository.sales.*;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ErpIntegrationService {
    private final ExternalInterfaceRepository repository;
    private final InterfaceTransactionService tx;
    private final ApprovalRepository approvals;
    private final SalesOpportunityRepository opportunities;
    private final CustomerRepository customers;
    private final ErpSalesPayloadFactory factory;
    private final ErpRestClient client;
    private final IntegrationPayloadSanitizer sanitizer;
    private final ErpIntegrationProperties properties;
    private final ObjectMapper mapper;
    private final CurrentEmployee current;
    private final AuditService audit;
    private final Clock clock;
    public ErpIntegrationService(ExternalInterfaceRepository repository, InterfaceTransactionService tx,
        ApprovalRepository approvals, SalesOpportunityRepository opportunities, CustomerRepository customers,
        ErpSalesPayloadFactory factory, ErpRestClient client, IntegrationPayloadSanitizer sanitizer,
        ErpIntegrationProperties properties, ObjectMapper mapper, CurrentEmployee current, AuditService audit,
        Clock clock) {
        this.repository = repository;
        this.tx = tx;
        this.approvals = approvals;
        this.opportunities = opportunities;
        this.customers = customers;
        this.factory = factory;
        this.client = client;
        this.sanitizer = sanitizer;
        this.properties = properties;
        this.mapper = mapper;
        this.current = current;
        this.audit = audit;
        this.clock = clock;
    }
    public InterfaceResponse sendApprovedOpportunity(Long opportunityId, ErpSimulationMode mode) {
        Long actor = current.requireEmployeeId();
        ApprovalRecord approval = latestApproved(opportunityId);
        return send(approval, actor, mode == null ? ErpSimulationMode.NORMAL : mode);
    }
    public InterfaceResponse sendFromApproval(Long approvalId, Long actor) {
        ApprovalRecord approval = approvals.findById(approvalId).orElseThrow(() -> new BusinessException(ErrorCode
            .APPROVAL_NOT_FOUND));
        if (approval.getStatus() != ApprovalStatus.APPROVED) throw new BusinessException(ErrorCode.INVALID_APPROVAL_TRANSITION);
        return send(approval, actor, ErpSimulationMode.NORMAL);
    }
    private InterfaceResponse send(ApprovalRecord approval, Long actor, ErpSimulationMode mode) {
        SalesOpportunityRecord o = opportunities.findById(approval.getBusinessId()).orElseThrow(() -> new BusinessException(ErrorCode
            .OPPORTUNITY_NOT_FOUND));
        CustomerRecord c = customers.findById(o.getCustomerId()).orElseThrow(() -> new BusinessException(ErrorCode
            .CUSTOMER_NOT_FOUND));
        String key = "WB:ERP_SALES:OPPORTUNITY:" + o.getOpportunityId() + ":v" + o.getVersion();
        Optional<ExternalInterfaceRecord> existing = repository.findByKey(key);
        if (existing.isPresent()) {
            ExternalInterfaceRecord e = existing.get();
            if (e.getStatus() == InterfaceStatus.SUCCESS) return new InterfaceResponse(e);
            if (e.getStatus().canRetry()) return retry(e.getInterfaceId(), mode, actor);
            return new InterfaceResponse(e);
        }
        ErpSalesPayload payload = factory.create(approval, o, c);
        String raw = json(payload);
        IntegrationPayloadSanitizer.Sanitized safe = sanitizer.sanitize(raw);
        ExternalInterfaceRecord r = new ExternalInterfaceRecord();
        r.setInterfaceType("ERP_SALES");
        r.setBusinessType("OPPORTUNITY");
        r.setBusinessId(o.getOpportunityId());
        r.setBusinessVersion(o.getVersion());
        r.setIdempotencyKey(key);
        r.setTraceId(trace());
        r.setStatus(InterfaceStatus.PENDING);
        r.setRequestMethod("POST");
        r.setRequestUrl(properties.getBaseUrl() + "/api/v1/erp/sales");
        r.setRequestHeaders("{\"Content-Type\":\"application/json\",\"Idempotency-Key\":\"" + key + "\"}");
        r.setRequestBody(safe.value());
        r.setRequestTruncated(safe.truncated());
        r.setCreatedBy(actor);
        try {
            tx.create(r);
        } catch (DuplicateKeyException e) {
            return new InterfaceResponse(repository.findByKey(key).orElseThrow());
        }
        execute(r, payload, mode, false);
        ExternalInterfaceRecord result = repository.find(r.getInterfaceId()).orElseThrow();
        audit.safeRecordIndependent(actor, "ERP_INTERFACE_SEND", "EXTERNAL_INTERFACE", String.valueOf(r
            .getInterfaceId()), null, Map.of("status", result.getStatus().name(), "idempotencyKey", key));
        return new InterfaceResponse(result);
    }
    public InterfaceResponse retry(Long id, ErpSimulationMode mode) {
        return retry(id, mode, current.requireEmployeeId());
    }
    private InterfaceResponse retry(Long id, ErpSimulationMode mode, Long actor) {
        ExternalInterfaceRecord r = repository.find(id).orElseThrow(() -> new BusinessException(ErrorCode
            .INTERFACE_NOT_FOUND));
        if (!r.getStatus().canRetry()) throw new BusinessException(r.getStatus() == InterfaceStatus.SUCCESS ? ErrorCode
            .INTERFACE_ALREADY_SUCCEEDED : ErrorCode.CONFLICT);
        SalesOpportunityRecord o = opportunities.findById(r.getBusinessId()).orElseThrow(() -> new BusinessException(ErrorCode
            .OPPORTUNITY_NOT_FOUND));
        if (o.getVersion() != r.getBusinessVersion()) throw new BusinessException(ErrorCode.INTERFACE_DATA_CHANGED);
        ApprovalRecord a = latestApproved(o.getOpportunityId());
        CustomerRecord c = customers.findById(o.getCustomerId()).orElseThrow(() -> new BusinessException(ErrorCode
            .CUSTOMER_NOT_FOUND));
        execute(r, factory.create(a, o, c), mode == null ? ErpSimulationMode.NORMAL : mode, true);
        ExternalInterfaceRecord result = repository.find(id).orElseThrow();
        audit.safeRecordIndependent(actor, "ERP_INTERFACE_RETRY", "EXTERNAL_INTERFACE", String.valueOf(id),
            Map.of("beforeStatus", r.getStatus().name()), Map.of("status", result.getStatus().name()));
        return new InterfaceResponse(result);
    }
    private void execute(ExternalInterfaceRecord r, ErpSalesPayload payload, ErpSimulationMode mode,
        boolean manual) {
        int firstRetry = r.getRetryCount() + (manual ? 1 : 0);
        int max = manual ? 1 :
        properties.getMaxAttempts();
        for (int index = 0; index<max; index++) {
            int retryCount = firstRetry + index;
            LocalDateTime start = LocalDateTime.now(clock);
            String trace = trace();
            if (!tx.processing(r.getInterfaceId(), manual ? InterfaceStatus.RETRYING : InterfaceStatus
                .PROCESSING, trace, start)) throw new BusinessException(ErrorCode.CONFLICT);
            try {
                ErpRestClient.ErpHttpResult result = client.send(payload, r.getIdempotencyKey(), mode,
                    trace);
                IntegrationPayloadSanitizer.Sanitized safe = sanitizer.sanitize(result.rawBody());
                tx.success(r.getInterfaceId(), result.statusCode(), safe.value(), safe.truncated(), retryCount,
                    start, LocalDateTime.now(clock), trace);
                return;
            } catch (ErpCallException e) {
                InterfaceStatus failure = e.isTemporary() ? InterfaceStatus.TEMPORARY_FAILED :
                InterfaceStatus.PERMANENT_FAILED;
                IntegrationPayloadSanitizer.Sanitized safe = sanitizer.sanitize(e.getResponseBody());
                tx.failure(r.getInterfaceId(), failure, e.getStatusCode(), safe.value(), safe.truncated(),
                    e.getCode(), truncate(e.getMessage(), 1000), retryCount, start, LocalDateTime.now(clock),
                    trace);
                if (!e.isRetryable() || index + 1 >= max) return;
                sleep(properties.getRetryBackoff());
            }
        }
    }
    @Transactional(readOnly = true)
    public PageResponse<InterfaceResponse> search(InterfaceStatus status, String traceId, int page, int size) {
        if (page<0 || size<1 || size> 100) throw new BusinessException(ErrorCode.INVALID_REQUEST);
        String trace = normalize(traceId);
        return PageResponse.of(repository.page(status, trace, page * size, size).stream().map(InterfaceResponse::new)
            .toList(), page, size, repository.count(status, trace));
    }
    @Transactional(readOnly = true)
    public InterfaceDetailResponse get(Long id) {
        ExternalInterfaceRecord r = repository.find(id).orElseThrow(() -> new BusinessException(ErrorCode
            .INTERFACE_NOT_FOUND));
        return new InterfaceDetailResponse(new InterfaceResponse(r), r.getRequestMethod(), r.getRequestUrl(),
            r.getRequestHeaders(), r.getRequestBody(), r.getResponseHeaders(), r.getResponseBody(), repository
            .attempts(id).stream().map(InterfaceAttemptResponse::new).toList());
    }
    private ApprovalRecord latestApproved(Long opportunityId) {
        return approvals.findPage("ALL", null, ApprovalStatus.APPROVED, null, null, 0, 100).stream()
            .filter(a -> "OPPORTUNITY".equals(a.getBusinessType()) && Objects.equals(a.getBusinessId(),
            opportunityId)).findFirst().orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPROVAL_TRANSITION,
            "승인된 결재가 있는 영업기회만 전송할 수 있습니다."));
    }
    private String json(Object v) {
        try {
            return mapper.writeValueAsString(v);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    private String trace() {
        String v = TraceIdContext.currentTraceId();
        return v == null ? UUID.randomUUID().toString().replace("-", "") :
        v;
    }
    private String normalize(String v) {
        return v == null || v.isBlank() ? null :
        v.trim();
    }
    private String truncate(String v, int n) {
        return v == null ? null :
        (v.length() <= n ? v : v.substring(0, n));
    }
    private void sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
