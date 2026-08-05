package com.workbridge.server.service.sales;

import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.OpportunityStatus;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;
import com.workbridge.server.domain.sales.SalesOpportunityUpdateCommand;
import com.workbridge.server.dto.sales.OpportunityCreateRequest;
import com.workbridge.server.dto.sales.OpportunityOptionResponse;
import com.workbridge.server.dto.sales.OpportunityResponse;
import com.workbridge.server.dto.sales.OpportunityUpdateRequest;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.sales.CustomerRepository;
import com.workbridge.server.repository.sales.SalesOpportunityRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesOpportunityService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int OPTION_LIMIT = 100;
    private static final int NUMBER_GENERATION_ATTEMPTS = 3;

    private final SalesOpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;
    private final SalesAccessPolicy salesAccessPolicy;
    private final OpportunityNumberGenerator numberGenerator;
    private final CurrentEmployee currentEmployee;
    private final AuditService auditService;

    public SalesOpportunityService(
            SalesOpportunityRepository opportunityRepository,
            CustomerRepository customerRepository,
            SalesAccessPolicy salesAccessPolicy,
            OpportunityNumberGenerator numberGenerator,
            CurrentEmployee currentEmployee,
            AuditService auditService
    ) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
        this.salesAccessPolicy = salesAccessPolicy;
        this.numberGenerator = numberGenerator;
        this.currentEmployee = currentEmployee;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<OpportunityResponse> search(
            String keyword,
            Long customerId,
            OpportunityStage stage,
            OpportunityStatus status,
            Long ownerEmployeeId,
            LocalDate closeDateFrom,
            LocalDate closeDateTo,
            int page,
            int size
    ) {
        validatePage(page, size);
        validateDateRange(closeDateFrom, closeDateTo);
        List<OpportunityResponse> items = opportunityRepository.findPage(
                        normalizeNullable(keyword), customerId, stage, status, ownerEmployeeId,
                        closeDateFrom, closeDateTo, page * size, size
                ).stream()
                .map(OpportunityResponse::new)
                .toList();
        long total = opportunityRepository.count(
                normalizeNullable(keyword), customerId, stage, status, ownerEmployeeId,
                closeDateFrom, closeDateTo
        );
        return PageResponse.of(items, page, size, total);
    }

    @Transactional(readOnly = true)
    public OpportunityResponse get(Long opportunityId) {
        return new OpportunityResponse(requireOpportunity(opportunityId));
    }

    @Transactional(readOnly = true)
    public List<OpportunityOptionResponse> getOptionsByCustomer(Long customerId) {
        requireCustomer(customerId);
        return opportunityRepository.findOptionsByCustomer(customerId, OPTION_LIMIT).stream()
                .map(OpportunityOptionResponse::new)
                .toList();
    }

    @Transactional
    public OpportunityResponse create(OpportunityCreateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        CustomerRecord customer = requireWritableCustomer(request.getCustomerId());
        salesAccessPolicy.requireCanModifyAssigned(customer.getAssignedSalesId());
        Long ownerEmployeeId = salesAccessPolicy.resolveAssigneeForCreate(request.getOwnerEmployeeId());
        validateStageAndProbability(request.getStage(), request.getProbability());

        SalesOpportunityRecord opportunity = new SalesOpportunityRecord();
        opportunity.setCustomerId(customer.getCustomerId());
        opportunity.setOpportunityName(request.getOpportunityName().trim());
        opportunity.setStage(request.getStage());
        opportunity.setExpectedRevenue(request.getExpectedRevenue());
        opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
        opportunity.setProbability(request.getProbability());
        opportunity.setOwnerEmployeeId(ownerEmployeeId);
        opportunity.setStatus(request.getStage().toStatus());
        opportunity.setDescription(normalizeNullable(request.getDescription()));

        insertWithGeneratedNumber(opportunity, actorId);
        OpportunityResponse created = get(opportunity.getOpportunityId());
        auditService.record(actorId, "OPPORTUNITY_CREATE", "SALES_OPPORTUNITY",
                String.valueOf(created.getOpportunityId()), null, auditData(created));
        return created;
    }

    @Transactional
    public OpportunityResponse update(Long opportunityId, OpportunityUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        SalesOpportunityRecord beforeRecord = requireOpportunity(opportunityId);
        requireWritableCustomer(beforeRecord.getCustomerId());
        salesAccessPolicy.requireCanModifyAssigned(beforeRecord.getOwnerEmployeeId());

        if (!request.getStage().canTransitionFrom(beforeRecord.getStage())) {
            throw new BusinessException(ErrorCode.INVALID_OPPORTUNITY_TRANSITION);
        }
        validateStageAndProbability(request.getStage(), request.getProbability());
        Long ownerEmployeeId = salesAccessPolicy.resolveAssigneeForUpdate(
                request.getOwnerEmployeeId(), beforeRecord.getOwnerEmployeeId());

        SalesOpportunityUpdateCommand command = new SalesOpportunityUpdateCommand(
                opportunityId,
                request.getOpportunityName().trim(),
                request.getStage(),
                request.getExpectedRevenue(),
                request.getExpectedCloseDate(),
                request.getProbability(),
                ownerEmployeeId,
                request.getStage().toStatus(),
                normalizeNullable(request.getDescription()),
                actorId,
                request.getVersion()
        );
        if (!opportunityRepository.update(command)) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }

        OpportunityResponse before = new OpportunityResponse(beforeRecord);
        OpportunityResponse after = get(opportunityId);
        auditService.record(actorId, "OPPORTUNITY_UPDATE", "SALES_OPPORTUNITY",
                String.valueOf(opportunityId), auditData(before), auditData(after));
        return after;
    }

    private void insertWithGeneratedNumber(SalesOpportunityRecord opportunity, Long actorId) {
        for (int attempt = 1; attempt <= NUMBER_GENERATION_ATTEMPTS; attempt += 1) {
            opportunity.setOpportunityNo(numberGenerator.next());
            try {
                opportunityRepository.insert(opportunity, actorId);
                return;
            } catch (DuplicateKeyException exception) {
                if (attempt == NUMBER_GENERATION_ATTEMPTS) {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                            "영업기회 번호 생성에 실패했습니다. 다시 시도해 주세요.");
                }
            }
        }
    }

    private CustomerRecord requireWritableCustomer(Long customerId) {
        CustomerRecord customer = requireCustomer(customerId);
        if (!customer.isActive() || customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.CUSTOMER_INACTIVE);
        }
        return customer;
    }

    private CustomerRecord requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private SalesOpportunityRecord requireOpportunity(Long opportunityId) {
        return opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPPORTUNITY_NOT_FOUND));
    }

    private void validateStageAndProbability(OpportunityStage stage, Integer probability) {
        if (stage == null || probability == null || !stage.acceptsProbability(probability)) {
            throw new BusinessException(ErrorCode.INVALID_OPPORTUNITY_PROBABILITY);
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "종료 예정일 시작값은 종료값보다 늦을 수 없습니다.");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "페이지 번호는 0 이상, 페이지 크기는 1~100이어야 합니다.");
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, Object> auditData(OpportunityResponse opportunity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("opportunityId", opportunity.getOpportunityId());
        data.put("opportunityNo", opportunity.getOpportunityNo());
        data.put("customerId", opportunity.getCustomerId());
        data.put("opportunityName", opportunity.getOpportunityName());
        data.put("stage", opportunity.getStage());
        data.put("status", opportunity.getStatus());
        data.put("expectedRevenue", opportunity.getExpectedRevenue());
        data.put("probability", opportunity.getProbability());
        data.put("ownerEmployeeId", opportunity.getOwnerEmployeeId());
        data.put("version", opportunity.getVersion());
        return data;
    }
}
