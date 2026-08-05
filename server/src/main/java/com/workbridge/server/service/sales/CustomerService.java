package com.workbridge.server.service.sales;

import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.audit.MaskingUtils;
import com.workbridge.server.domain.sales.BusinessRegistrationNumber;
import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.domain.sales.CustomerUpdateCommand;
import com.workbridge.server.dto.sales.CustomerCreateRequest;
import com.workbridge.server.dto.sales.CustomerOptionResponse;
import com.workbridge.server.dto.sales.CustomerResponse;
import com.workbridge.server.dto.sales.CustomerUpdateRequest;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.sales.CustomerRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CustomerService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int OPTION_LIMIT = 50;

    private final CustomerRepository customerRepository;
    private final SalesAccessPolicy salesAccessPolicy;
    private final CurrentEmployee currentEmployee;
    private final AuditService auditService;

    public CustomerService(
            CustomerRepository customerRepository,
            SalesAccessPolicy salesAccessPolicy,
            CurrentEmployee currentEmployee,
            AuditService auditService
    ) {
        this.customerRepository = customerRepository;
        this.salesAccessPolicy = salesAccessPolicy;
        this.currentEmployee = currentEmployee;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(
            String customerName,
            String businessRegistrationNo,
            String contactName,
            Long assignedSalesId,
            CustomerStatus status,
            Boolean active,
            int page,
            int size
    ) {
        validatePage(page, size);
        String normalizedBusinessNo = normalizeBusinessSearch(businessRegistrationNo);
        List<CustomerResponse> items = customerRepository.findPage(
                        normalizeNullable(customerName),
                        normalizedBusinessNo,
                        normalizeNullable(contactName),
                        assignedSalesId,
                        status,
                        active,
                        page * size,
                        size
                ).stream()
                .map(CustomerResponse::new)
                .toList();
        long total = customerRepository.count(
                normalizeNullable(customerName),
                normalizedBusinessNo,
                normalizeNullable(contactName),
                assignedSalesId,
                status,
                active
        );
        return PageResponse.of(items, page, size, total);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long customerId) {
        return new CustomerResponse(requireCustomer(customerId));
    }

    @Transactional(readOnly = true)
    public List<CustomerOptionResponse> getOptions(String keyword) {
        String normalized = normalizeNullable(keyword);
        if (normalized != null && BusinessRegistrationNumber.hasSupportedCharacters(normalized)) {
            normalized = BusinessRegistrationNumber.normalize(normalized);
        }
        return customerRepository.findOptions(normalized, OPTION_LIMIT).stream()
                .map(CustomerOptionResponse::new)
                .toList();
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        String businessNo = requireBusinessRegistrationNumber(request.getBusinessRegistrationNo());
        if (customerRepository.existsByBusinessRegistrationNo(businessNo)) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION_NO);
        }

        Long assignedSalesId = salesAccessPolicy.resolveAssigneeForCreate(request.getAssignedSalesId());
        CustomerStatus status = normalizeStatus(request.getStatus(), request.isActive());
        boolean active = request.isActive() && status != CustomerStatus.INACTIVE;

        CustomerRecord customer = new CustomerRecord(
                null,
                businessNo,
                request.getCustomerName().trim(),
                normalizeNullable(request.getRepresentativeName()),
                normalizeNullable(request.getContactName()),
                normalizeNullable(request.getContactPhone()),
                normalizeEmail(request.getContactEmail()),
                request.getEstablishedDate(),
                assignedSalesId,
                null,
                null,
                status,
                active,
                null,
                null,
                0
        );

        try {
            customerRepository.insert(customer, actorId);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION_NO);
        }

        CustomerResponse created = get(customer.getCustomerId());
        auditService.record(actorId, "CUSTOMER_CREATE", "CUSTOMER",
                String.valueOf(created.getCustomerId()), null, auditData(created));
        return created;
    }

    @Transactional
    public CustomerResponse update(Long customerId, CustomerUpdateRequest request) {
        Long actorId = currentEmployee.requireEmployeeId();
        CustomerRecord beforeRecord = requireCustomer(customerId);
        salesAccessPolicy.requireCanModifyAssigned(beforeRecord.getAssignedSalesId());

        String businessNo = requireBusinessRegistrationNumber(request.getBusinessRegistrationNo());
        if (customerRepository.existsByBusinessRegistrationNoExcludingCustomer(businessNo, customerId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION_NO);
        }

        Long assignedSalesId = salesAccessPolicy.resolveAssigneeForUpdate(
                request.getAssignedSalesId(), beforeRecord.getAssignedSalesId());
        CustomerStatus status = normalizeStatus(request.getStatus(), request.isActive());
        boolean active = request.isActive() && status != CustomerStatus.INACTIVE;

        CustomerUpdateCommand command = new CustomerUpdateCommand(
                customerId,
                businessNo,
                request.getCustomerName().trim(),
                normalizeNullable(request.getRepresentativeName()),
                normalizeNullable(request.getContactName()),
                normalizeNullable(request.getContactPhone()),
                normalizeEmail(request.getContactEmail()),
                request.getEstablishedDate(),
                assignedSalesId,
                status,
                active,
                actorId,
                request.getVersion()
        );

        try {
            if (!customerRepository.update(command)) {
                throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_REGISTRATION_NO);
        }

        CustomerResponse before = new CustomerResponse(beforeRecord);
        CustomerResponse after = get(customerId);
        auditService.record(actorId, "CUSTOMER_UPDATE", "CUSTOMER", String.valueOf(customerId),
                auditData(before), auditData(after));
        return after;
    }

    private CustomerRecord requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private String requireBusinessRegistrationNumber(String value) {
        if (!BusinessRegistrationNumber.isValid(value)) {
            throw new BusinessException(ErrorCode.INVALID_BUSINESS_REGISTRATION_NO);
        }
        return BusinessRegistrationNumber.normalize(value);
    }

    private String normalizeBusinessSearch(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        if (!BusinessRegistrationNumber.hasSupportedCharacters(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_BUSINESS_REGISTRATION_NO,
                    "사업자번호 검색은 숫자, 공백, 하이픈만 입력할 수 있습니다.");
        }
        String digits = BusinessRegistrationNumber.normalize(normalized);
        if (digits.isEmpty() || digits.length() > 10) {
            throw new BusinessException(ErrorCode.INVALID_BUSINESS_REGISTRATION_NO);
        }
        return digits;
    }

    private CustomerStatus normalizeStatus(CustomerStatus requestedStatus, boolean active) {
        if (!active || requestedStatus == CustomerStatus.INACTIVE) {
            return CustomerStatus.INACTIVE;
        }
        return requestedStatus == null ? CustomerStatus.PROSPECT : requestedStatus;
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
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

    private Map<String, Object> auditData(CustomerResponse customer) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customerId", customer.getCustomerId());
        data.put("businessRegistrationNo", MaskingUtils.maskRejectedValue(
                "businessRegistrationNo", customer.getBusinessRegistrationNo()));
        data.put("customerName", customer.getCustomerName());
        data.put("assignedSalesId", customer.getAssignedSalesId());
        data.put("status", customer.getStatus());
        data.put("active", customer.isActive());
        data.put("contactEmail", MaskingUtils.maskRejectedValue(
                "contactEmail", customer.getContactEmail()));
        data.put("contactPhone", MaskingUtils.maskRejectedValue(
                "contactPhone", customer.getContactPhone()));
        data.put("version", customer.getVersion());
        return data;
    }
}
