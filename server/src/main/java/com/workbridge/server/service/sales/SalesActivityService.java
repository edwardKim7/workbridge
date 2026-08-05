package com.workbridge.server.service.sales;

import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.domain.sales.SalesActivityRecord;
import com.workbridge.server.domain.sales.SalesActivityType;
import com.workbridge.server.domain.sales.SalesActivityUpdateCommand;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;
import com.workbridge.server.dto.sales.SalesActivityCreateRequest;
import com.workbridge.server.dto.sales.SalesActivityResponse;
import com.workbridge.server.dto.sales.SalesActivityUpdateRequest;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.sales.CustomerRepository;
import com.workbridge.server.repository.sales.SalesActivityRepository;
import com.workbridge.server.repository.sales.SalesOpportunityRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SalesActivityService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SalesActivityRepository activityRepository;
    private final CustomerRepository customerRepository;
    private final SalesOpportunityRepository opportunityRepository;
    private final SalesAccessPolicy salesAccessPolicy;
    private final CurrentEmployee currentEmployee;
    private final AuditService auditService;

    public SalesActivityService(
            SalesActivityRepository activityRepository,
            CustomerRepository customerRepository,
            SalesOpportunityRepository opportunityRepository,
            SalesAccessPolicy salesAccessPolicy,
            CurrentEmployee currentEmployee,
            AuditService auditService
    ) {
        this.activityRepository = activityRepository;
        this.customerRepository = customerRepository;
        this.opportunityRepository = opportunityRepository;
        this.salesAccessPolicy = salesAccessPolicy;
        this.currentEmployee = currentEmployee;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SalesActivityResponse> search(
            String keyword,
            Long customerId,
            Long opportunityId,
            Long employeeId,
            SalesActivityType activityType,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        validatePage(page, size);
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "활동일시 시작값은 종료값보다 늦을 수 없습니다.");
        }
        List<SalesActivityResponse> items = activityRepository.findPage(
                        normalizeNullable(keyword), customerId, opportunityId, employeeId, activityType,
                        from, to, page * size, size
                ).stream()
                .map(SalesActivityResponse::new)
                .toList();
        long total = activityRepository.count(
                normalizeNullable(keyword), customerId, opportunityId, employeeId, activityType, from, to);
        return PageResponse.of(items, page, size, total);
    }

    @Transactional(readOnly = true)
    public SalesActivityResponse get(Long activityId) {
        return new SalesActivityResponse(requireActivity(activityId));
    }

    @Transactional
    public SalesActivityResponse create(SalesActivityCreateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        CustomerRecord customer = requireActiveCustomer(request.getCustomerId());
        salesAccessPolicy.requireCanModifyAssigned(customer.getAssignedSalesId());
        SalesOpportunityRecord opportunity = validateOpportunityTarget(
                customer.getCustomerId(), request.getOpportunityId());
        if (opportunity != null) {
            salesAccessPolicy.requireCanModifyAssigned(opportunity.getOwnerEmployeeId());
        }
        Long employeeId = salesAccessPolicy.resolveAssigneeForCreate(request.getEmployeeId());

        SalesActivityRecord activity = new SalesActivityRecord();
        activity.setCustomerId(customer.getCustomerId());
        activity.setOpportunityId(request.getOpportunityId());
        activity.setEmployeeId(employeeId);
        activity.setActivityType(request.getActivityType());
        activity.setSubject(request.getSubject().trim());
        activity.setActivityAt(request.getActivityAt());
        activity.setLocation(normalizeNullable(request.getLocation()));
        activity.setContent(normalizeNullable(request.getContent()));
        activityRepository.insert(activity, actorId);

        SalesActivityResponse created = get(activity.getActivityId());
        auditService.record(actorId, "SALES_ACTIVITY_CREATE", "SALES_ACTIVITY",
                String.valueOf(created.getActivityId()), null, auditData(created));
        return created;
    }

    @Transactional
    public SalesActivityResponse update(Long activityId, SalesActivityUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        SalesActivityRecord beforeRecord = requireActivity(activityId);
        CustomerRecord customer = requireActiveCustomer(beforeRecord.getCustomerId());
        salesAccessPolicy.requireCanModifyAssigned(customer.getAssignedSalesId());
        SalesOpportunityRecord opportunity = validateOpportunityTarget(
                customer.getCustomerId(), request.getOpportunityId());
        if (opportunity != null) {
            salesAccessPolicy.requireCanModifyAssigned(opportunity.getOwnerEmployeeId());
        }
        Long employeeId = salesAccessPolicy.resolveAssigneeForUpdate(
                request.getEmployeeId(), beforeRecord.getEmployeeId());

        SalesActivityUpdateCommand command = new SalesActivityUpdateCommand(
                activityId,
                request.getOpportunityId(),
                employeeId,
                request.getActivityType(),
                request.getSubject().trim(),
                request.getActivityAt(),
                normalizeNullable(request.getLocation()),
                normalizeNullable(request.getContent()),
                actorId,
                request.getVersion()
        );
        if (!activityRepository.update(command)) {
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }

        SalesActivityResponse before = new SalesActivityResponse(beforeRecord);
        SalesActivityResponse after = get(activityId);
        auditService.record(actorId, "SALES_ACTIVITY_UPDATE", "SALES_ACTIVITY",
                String.valueOf(activityId), auditData(before), auditData(after));
        return after;
    }

    private CustomerRecord requireActiveCustomer(Long customerId) {
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

    private SalesOpportunityRecord validateOpportunityTarget(Long customerId, Long opportunityId) {
        if (opportunityId == null) {
            return null;
        }
        SalesOpportunityRecord opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPPORTUNITY_NOT_FOUND));
        if (!Objects.equals(customerId, opportunity.getCustomerId())) {
            throw new BusinessException(ErrorCode.ACTIVITY_TARGET_MISMATCH);
        }
        return opportunity;
    }

    private SalesActivityRecord requireActivity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALES_ACTIVITY_NOT_FOUND));
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "페이지 번호는 0 이상, 페이지 크기는 1~100이어야 합니다.");
        }
    }

    private Map<String, Object> auditData(SalesActivityResponse activity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("activityId", activity.getActivityId());
        data.put("customerId", activity.getCustomerId());
        data.put("opportunityId", activity.getOpportunityId());
        data.put("employeeId", activity.getEmployeeId());
        data.put("activityType", activity.getActivityType());
        data.put("subject", activity.getSubject());
        data.put("activityAt", activity.getActivityAt());
        data.put("version", activity.getVersion());
        return data;
    }
}
