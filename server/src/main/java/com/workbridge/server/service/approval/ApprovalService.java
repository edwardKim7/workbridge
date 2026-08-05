package com.workbridge.server.service.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.audit.TraceIdContext;
import com.workbridge.server.domain.approval.*;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;
import com.workbridge.server.dto.approval.*;
import com.workbridge.server.exception.*;
import com.workbridge.server.repository.approval.ApprovalRepository;
import com.workbridge.server.repository.sales.SalesOpportunityRepository;
import com.workbridge.server.security.*;
import com.workbridge.server.service.audit.AuditService;
import com.workbridge.server.service.sales.SalesAccessPolicy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class ApprovalService {
    private static final int MAX_PAGE_SIZE = 100;
    private final ApprovalRepository repository;
    private final SalesOpportunityRepository opportunityRepository;
    private final SalesAccessPolicy salesAccessPolicy;
    private final CurrentEmployee currentEmployee;
    private final ApprovalNumberGenerator numberGenerator;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;
    public ApprovalService(ApprovalRepository repository, SalesOpportunityRepository opportunityRepository,
        SalesAccessPolicy salesAccessPolicy, CurrentEmployee currentEmployee, ApprovalNumberGenerator numberGenerator,
        ObjectMapper objectMapper, AuditService auditService, ApplicationEventPublisher publisher, Clock clock) {
        this.repository = repository;
        this.opportunityRepository = opportunityRepository;
        this.salesAccessPolicy = salesAccessPolicy;
        this.currentEmployee = currentEmployee;
        this.numberGenerator = numberGenerator;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.publisher = publisher;
        this.clock = clock;
    }
    @Transactional(readOnly = true)
    public PageResponse<ApprovalResponse> search(ApprovalView view, ApprovalStatus status, ApprovalType type,
        String keyword, int page, int size) {
        validatePage(page, size);
        WorkBridgePrincipal p = currentEmployee.requirePrincipal();
        String scope = scope(view, p);
        List<ApprovalResponse> items = repository.findPage(scope, p.getEmployeeId(), status, type, normalize(keyword),
            page * size, size).stream().map(this::toResponse).toList();
        return PageResponse.of(items, page, size, repository.count(scope, p.getEmployeeId(), status,
            type, normalize(keyword)));
    }
    @Transactional(readOnly = true)
    public ApprovalDetailResponse get(Long id) {
        ApprovalRecord r = requireVisible(id);
        WorkBridgePrincipal p = currentEmployee.requirePrincipal();
        boolean requester = Objects.equals(r.getRequesterId(), p.getEmployeeId());
        boolean approver = Objects.equals(r.getApproverId(), p.getEmployeeId());
        boolean draft = r.getStatus() == ApprovalStatus.DRAFT;
        boolean requested = r.getStatus() == ApprovalStatus.REQUESTED;
        return new ApprovalDetailResponse(toResponse(r), repository.findHistories(id).stream().map(ApprovalHistoryResponse::new)
            .toList(), draft && requester, draft && requester, requested && approver, requested && approver,
            draft && requester);
    }
    @Transactional(readOnly = true)
    public List<ApproverOptionResponse> approvers() {
        return repository.findActiveApprovers();
    }
    @Transactional
    public ApprovalDetailResponse create(ApprovalCreateRequest request) {
        WorkBridgePrincipal p = currentEmployee.requirePrincipal();
        requireRequesterRole(p);
        SalesOpportunityRecord o = requireOpportunity(request.businessId());
        if (p.hasRole("SALES")) salesAccessPolicy.requireCanModifyAssigned(o.getOwnerEmployeeId());
        validateApprover(request.approverId(), p.getEmployeeId());
        ApprovalPayload payload = payload(request.amount(), request.expenseCategory(), request.visitDate(),
            request.discountRate(), request.description());
        validatePayload(request.approvalType(), payload);
        ApprovalRecord r = new ApprovalRecord();
        r.setApprovalNo(numberGenerator.next());
        r.setApprovalType(request.approvalType());
        r.setBusinessType("OPPORTUNITY");
        r.setBusinessId(o.getOpportunityId());
        r.setRequesterId(p.getEmployeeId());
        r.setApproverId(request.approverId());
        r.setTitle(request.title().trim());
        r.setRequestPayload(json(payload));
        r.setStatus(ApprovalStatus.DRAFT);
        repository.insert(r, p.getEmployeeId());
        LocalDateTime now = LocalDateTime.now(clock);
        repository.history(r.getApprovalId(), null, ApprovalStatus.DRAFT, ApprovalAction.CREATED, p.getEmployeeId(),
            null, TraceIdContext.currentTraceId(), now);
        auditService.record(p.getEmployeeId(), "APPROVAL_CREATE", "APPROVAL_REQUEST", String.valueOf(r
            .getApprovalId()), null, Map.of("approvalNo", r.getApprovalNo(), "title", r.getTitle()));
        return get(r.getApprovalId());
    }
    @Transactional
    public ApprovalDetailResponse update(Long id, ApprovalUpdateRequest request) {
        WorkBridgePrincipal p = currentEmployee.requirePrincipal();
        ApprovalRecord before = require(id);
        requireRequesterDraft(before, p.getEmployeeId());
        SalesOpportunityRecord o = requireOpportunity(request.businessId());
        if (p.hasRole("SALES")) salesAccessPolicy.requireCanModifyAssigned(o.getOwnerEmployeeId());
        validateApprover(request.approverId(), p.getEmployeeId());
        ApprovalPayload payload = payload(request.amount(), request.expenseCategory(), request.visitDate(),
            request.discountRate(), request.description());
        validatePayload(request.approvalType(), payload);
        boolean ok = repository.updateDraft(new ApprovalUpdateCommand(id, request.approvalType(), request
            .businessId(), request.approverId(), request.title().trim(), json(payload), p.getEmployeeId(),
            request.version()));
        if (!ok) throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        LocalDateTime now = LocalDateTime.now(clock);
        repository.history(id, ApprovalStatus.DRAFT, ApprovalStatus.DRAFT, ApprovalAction.UPDATED, p
            .getEmployeeId(), null, TraceIdContext.currentTraceId(), now);
        auditService.record(p.getEmployeeId(), "APPROVAL_UPDATE", "APPROVAL_REQUEST", String.valueOf(id),
            Map.of("version", before.getVersion()), Map.of("version", request.version() + 1));
        return get(id);
    }
    @Transactional
    public ApprovalDetailResponse request(Long id, ApprovalTransitionRequest req) {
        return transitionDraft(id, req, ApprovalStatus.REQUESTED, ApprovalAction.REQUESTED);
    }
    @Transactional
    public ApprovalDetailResponse cancel(Long id, ApprovalTransitionRequest req) {
        return transitionDraft(id, req, ApprovalStatus.CANCELED, ApprovalAction.CANCELED);
    }
    private ApprovalDetailResponse transitionDraft(Long id, ApprovalTransitionRequest req, ApprovalStatus target,
        ApprovalAction action) {
        Long actor = currentEmployee.requireEmployeeId();
        ApprovalRecord before = require(id);
        requireRequesterDraft(before, actor);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!repository.transitionDraft(id, actor, target, actor, req.version(), now)) throw new BusinessException(ErrorCode
            .APPROVAL_ALREADY_PROCESSED);
        repository.history(id, ApprovalStatus.DRAFT, target, action, actor, normalize(req.opinion()),
            TraceIdContext.currentTraceId(), now);
        auditService.record(actor, "APPROVAL_" + action.name(), "APPROVAL_REQUEST", String.valueOf(id),
            Map.of("status", "DRAFT"), Map.of("status", target.name()));
        return get(id);
    }
    @Transactional
    public ApprovalDetailResponse approve(Long id, ApprovalTransitionRequest req) {
        return process(id, req, ApprovalStatus.APPROVED, ApprovalAction.APPROVED);
    }
    @Transactional
    public ApprovalDetailResponse reject(Long id, ApprovalTransitionRequest req) {
        if (normalize(req.opinion()) == null) throw new BusinessException(ErrorCode.APPROVAL_REJECTION_OPINION_REQUIRED);
        return process(id, req, ApprovalStatus.REJECTED, ApprovalAction.REJECTED);
    }
    private ApprovalDetailResponse process(Long id, ApprovalTransitionRequest req, ApprovalStatus target,
        ApprovalAction action) {
        Long actor = currentEmployee.requireEmployeeId();
        ApprovalRecord before = require(id);
        if (before.getStatus() != ApprovalStatus.REQUESTED || !Objects.equals(before.getApproverId(),
            actor)) throw new BusinessException(ErrorCode.APPROVAL_ALREADY_PROCESSED);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!repository.process(id, actor, target, actor, req.version(), now)) throw new BusinessException(ErrorCode
            .APPROVAL_ALREADY_PROCESSED);
        repository.history(id, ApprovalStatus.REQUESTED, target, action, actor, normalize(req.opinion()),
            TraceIdContext.currentTraceId(), now);
        auditService.record(actor, "APPROVAL_" + action.name(), "APPROVAL_REQUEST", String.valueOf(id),
            Map.of("status", "REQUESTED"), Map.of("status", target.name()));
        if (target == ApprovalStatus.APPROVED) publisher.publishEvent(new ApprovalCompletedEvent(id,
            actor));
        return get(id);
    }
    private ApprovalRecord requireVisible(Long id) {
        ApprovalRecord r = require(id);
        WorkBridgePrincipal p = currentEmployee.requirePrincipal();
        if (p.hasRole("ADMIN") || p.hasRole("VIEWER") || Objects.equals(r.getRequesterId(), p.getEmployeeId()) || Objects
            .equals(r.getApproverId(), p.getEmployeeId())) return r;
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    private ApprovalRecord require(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));
    }
    private SalesOpportunityRecord requireOpportunity(Long id) {
        return opportunityRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.OPPORTUNITY_NOT_FOUND));
    }
    private void requireRequesterRole(WorkBridgePrincipal p) {
        if (!p.hasRole("SALES") && !p.hasRole("ADMIN")) throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    private void requireRequesterDraft(ApprovalRecord r, Long actor) {
        if (r.getStatus() != ApprovalStatus.DRAFT) throw new BusinessException(ErrorCode.INVALID_APPROVAL_TRANSITION);
        if (!Objects.equals(r.getRequesterId(), actor)) throw new BusinessException(ErrorCode.APPROVAL_CANCEL_NOT_ALLOWED);
    }
    private void validateApprover(Long approver, Long requester) {
        if (Objects.equals(approver, requester)) throw new BusinessException(ErrorCode.APPROVAL_SELF_PROCESSING_NOT_ALLOWED);
        if (!repository.isActiveApprover(approver)) throw new BusinessException(ErrorCode.INVALID_REQUEST,
            "활성 APPROVER 역할 직원을 지정해 주세요.");
    }
    private void validatePayload(ApprovalType type, ApprovalPayload p) {
        switch (type) {
            case SALES_EXPENSE -> {
                if (p.amount() == null || p.amount().compareTo(BigDecimal.ZERO) <= 0 || p.expenseCategory() == null) throw new BusinessException(ErrorCode
                    .INVALID_REQUEST, "영업비용 금액과 비용 분류를 입력해 주세요.");
            }
            case VISIT_PLAN -> {
                if (p.visitDate() == null) throw new BusinessException(ErrorCode.INVALID_REQUEST, "방문 예정일을 입력해 주세요.");
            }
            case DISCOUNT_EXCEPTION -> {
                if (p.discountRate() == null || p.discountRate().compareTo(BigDecimal.ZERO) <= 0 || p
                    .discountRate().compareTo(new BigDecimal("100"))> 0) throw new BusinessException(ErrorCode
                    .INVALID_REQUEST, "예외 할인율을 0 초과 100 이하로 입력해 주세요.");
            }
        }
    }
    private ApprovalPayload payload(BigDecimal a, String c, LocalDate v, BigDecimal d, String desc) {
        return new ApprovalPayload(a, normalize(c), v, d, normalize(desc));
    }
    private String json(Object v) {
        try {
            return objectMapper.writeValueAsString(v);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    private ApprovalPayload parse(String v) {
        try {
            return objectMapper.readValue(v, ApprovalPayload.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "저장된 결재 상세 정보를 읽을 수 없습니다.");
        }
    }
    private ApprovalResponse toResponse(ApprovalRecord r) {
        return new ApprovalResponse(r.getApprovalId(), r.getApprovalNo(), r.getApprovalType(), r.getBusinessType(),
            r.getBusinessId(), r.getBusinessName(), r.getRequesterId(), r.getRequesterName(), r.getApproverId(),
            r.getApproverName(), r.getTitle(), parse(r.getRequestPayload()), r.getStatus(), r.getRequestedAt(),
            r.getProcessedAt(), r.getCanceledAt(), r.getCreatedAt(), r.getUpdatedAt(), r.getVersion());
    }
    private String scope(ApprovalView view, WorkBridgePrincipal p) {
        ApprovalView v = view == null ? (p.hasRole("APPROVER") ? ApprovalView.MY_APPROVALS : ApprovalView
            .MY_REQUESTS) :
        view;
        return switch (v) {
            case MY_REQUESTS -> "REQUESTER";
            case MY_APPROVALS -> "APPROVER";
            case ALL -> {
                if (!p.hasRole("ADMIN") && !p.hasRole("VIEWER")) throw new BusinessException(ErrorCode
                    .ACCESS_DENIED);
                yield "ALL";
            }
        };
    }
    private void validatePage(int p, int s) {
        if (p<0 || s<1 || s> MAX_PAGE_SIZE) throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상, size는 1~100이어야 합니다.");
    }
    private String normalize(String s) {
        return s == null || s.isBlank() ? null :
        s.trim();
    }
}
