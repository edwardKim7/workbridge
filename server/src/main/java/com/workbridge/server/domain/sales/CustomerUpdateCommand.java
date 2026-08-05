package com.workbridge.server.domain.sales;

import java.time.LocalDate;

public class CustomerUpdateCommand {

    private final Long customerId;
    private final String businessRegistrationNo;
    private final String customerName;
    private final String representativeName;
    private final String contactName;
    private final String contactPhone;
    private final String contactEmail;
    private final LocalDate establishedDate;
    private final Long assignedSalesId;
    private final CustomerStatus status;
    private final boolean active;
    private final Long actorId;
    private final long version;

    public CustomerUpdateCommand(
            Long customerId,
            String businessRegistrationNo,
            String customerName,
            String representativeName,
            String contactName,
            String contactPhone,
            String contactEmail,
            LocalDate establishedDate,
            Long assignedSalesId,
            CustomerStatus status,
            boolean active,
            Long actorId,
            long version
    ) {
        this.customerId = customerId;
        this.businessRegistrationNo = businessRegistrationNo;
        this.customerName = customerName;
        this.representativeName = representativeName;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.establishedDate = establishedDate;
        this.assignedSalesId = assignedSalesId;
        this.status = status;
        this.active = active;
        this.actorId = actorId;
        this.version = version;
    }

    public Long getCustomerId() { return customerId; }
    public String getBusinessRegistrationNo() { return businessRegistrationNo; }
    public String getCustomerName() { return customerName; }
    public String getRepresentativeName() { return representativeName; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public LocalDate getEstablishedDate() { return establishedDate; }
    public Long getAssignedSalesId() { return assignedSalesId; }
    public CustomerStatus getStatus() { return status; }
    public boolean isActive() { return active; }
    public Long getActorId() { return actorId; }
    public long getVersion() { return version; }
}
