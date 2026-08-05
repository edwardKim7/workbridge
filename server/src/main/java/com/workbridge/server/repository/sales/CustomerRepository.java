package com.workbridge.server.repository.sales;

import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.domain.sales.CustomerUpdateCommand;
import com.workbridge.server.mapper.sales.CustomerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final CustomerMapper mapper;

    public CustomerRepository(CustomerMapper mapper) {
        this.mapper = mapper;
    }

    public List<CustomerRecord> findPage(
            String customerName,
            String businessRegistrationNo,
            String contactName,
            Long assignedSalesId,
            CustomerStatus status,
            Boolean active,
            int offset,
            int size
    ) {
        return mapper.selectPage(customerName, businessRegistrationNo, contactName,
                assignedSalesId, status, active, offset, size);
    }

    public long count(
            String customerName,
            String businessRegistrationNo,
            String contactName,
            Long assignedSalesId,
            CustomerStatus status,
            Boolean active
    ) {
        return mapper.countPage(customerName, businessRegistrationNo, contactName,
                assignedSalesId, status, active);
    }

    public Optional<CustomerRecord> findById(Long customerId) {
        return Optional.ofNullable(mapper.selectById(customerId));
    }

    public List<CustomerRecord> findOptions(String keyword, int limit) {
        return mapper.selectOptions(keyword, limit);
    }

    public boolean existsByBusinessRegistrationNo(String businessRegistrationNo) {
        return mapper.countByBusinessRegistrationNo(businessRegistrationNo) > 0;
    }

    public boolean existsByBusinessRegistrationNoExcludingCustomer(
            String businessRegistrationNo,
            Long customerId
    ) {
        return mapper.countByBusinessRegistrationNoExcludingCustomer(
                businessRegistrationNo, customerId) > 0;
    }

    public Long insert(CustomerRecord customer, Long actorId) {
        mapper.insert(customer, actorId);
        return customer.getCustomerId();
    }

    public boolean update(CustomerUpdateCommand command) {
        return mapper.update(command) == 1;
    }
}
