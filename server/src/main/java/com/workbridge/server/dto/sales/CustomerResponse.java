package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.BusinessRegistrationNumber;
import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponse {

    private final Long customerId;
    private final String businessRegistrationNo;
    private final String formattedBusinessRegistrationNo;
    private final String customerName;
    private final String representativeName;
    private final String contactName;
    private final String contactPhone;
    private final String contactEmail;
    private final LocalDate establishedDate;
    private final Long assignedSalesId;
    private final String assignedSalesEmployeeNo;
    private final String assignedSalesName;
    private final CustomerStatus status;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final long version;

    public CustomerResponse(CustomerRecord customer) {
        this.customerId = customer.getCustomerId();
        this.businessRegistrationNo = customer.getBusinessRegistrationNo();
        this.formattedBusinessRegistrationNo = BusinessRegistrationNumber.format(customer.getBusinessRegistrationNo());
        this.customerName = customer.getCustomerName();
        this.representativeName = customer.getRepresentativeName();
        this.contactName = customer.getContactName();
        this.contactPhone = customer.getContactPhone();
        this.contactEmail = customer.getContactEmail();
        this.establishedDate = customer.getEstablishedDate();
        this.assignedSalesId = customer.getAssignedSalesId();
        this.assignedSalesEmployeeNo = customer.getAssignedSalesEmployeeNo();
        this.assignedSalesName = customer.getAssignedSalesName();
        this.status = customer.getStatus();
        this.active = customer.isActive();
        this.createdAt = customer.getCreatedAt();
        this.updatedAt = customer.getUpdatedAt();
        this.version = customer.getVersion();
    }

    public Long getCustomerId() { return customerId; }
    public String getBusinessRegistrationNo() { return businessRegistrationNo; }
    public String getFormattedBusinessRegistrationNo() { return formattedBusinessRegistrationNo; }
    public String getCustomerName() { return customerName; }
    public String getRepresentativeName() { return representativeName; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public LocalDate getEstablishedDate() { return establishedDate; }
    public Long getAssignedSalesId() { return assignedSalesId; }
    public String getAssignedSalesEmployeeNo() { return assignedSalesEmployeeNo; }
    public String getAssignedSalesName() { return assignedSalesName; }
    public CustomerStatus getStatus() { return status; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
