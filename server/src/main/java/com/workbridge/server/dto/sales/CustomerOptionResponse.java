package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.BusinessRegistrationNumber;
import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;

public class CustomerOptionResponse {

    private final Long customerId;
    private final String customerName;
    private final String businessRegistrationNo;
    private final String formattedBusinessRegistrationNo;
    private final Long assignedSalesId;
    private final String assignedSalesName;
    private final CustomerStatus status;
    private final boolean active;

    public CustomerOptionResponse(CustomerRecord customer) {
        this.customerId = customer.getCustomerId();
        this.customerName = customer.getCustomerName();
        this.businessRegistrationNo = customer.getBusinessRegistrationNo();
        this.formattedBusinessRegistrationNo = BusinessRegistrationNumber.format(
                customer.getBusinessRegistrationNo());
        this.assignedSalesId = customer.getAssignedSalesId();
        this.assignedSalesName = customer.getAssignedSalesName();
        this.status = customer.getStatus();
        this.active = customer.isActive();
    }

    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getBusinessRegistrationNo() { return businessRegistrationNo; }
    public String getFormattedBusinessRegistrationNo() { return formattedBusinessRegistrationNo; }
    public Long getAssignedSalesId() { return assignedSalesId; }
    public String getAssignedSalesName() { return assignedSalesName; }
    public CustomerStatus getStatus() { return status; }
    public boolean isActive() { return active; }
}
