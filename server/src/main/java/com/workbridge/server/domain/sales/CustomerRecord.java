package com.workbridge.server.domain.sales;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerRecord {

    private Long customerId;
    private String businessRegistrationNo;
    private String customerName;
    private String representativeName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private LocalDate establishedDate;
    private Long assignedSalesId;
    private String assignedSalesEmployeeNo;
    private String assignedSalesName;
    private CustomerStatus status;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public CustomerRecord() {
    }

    public CustomerRecord(
            Long customerId,
            String businessRegistrationNo,
            String customerName,
            String representativeName,
            String contactName,
            String contactPhone,
            String contactEmail,
            LocalDate establishedDate,
            Long assignedSalesId,
            String assignedSalesEmployeeNo,
            String assignedSalesName,
            CustomerStatus status,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
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
        this.assignedSalesEmployeeNo = assignedSalesEmployeeNo;
        this.assignedSalesName = assignedSalesName;
        this.status = status;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getBusinessRegistrationNo() { return businessRegistrationNo; }
    public void setBusinessRegistrationNo(String businessRegistrationNo) { this.businessRegistrationNo = businessRegistrationNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getRepresentativeName() { return representativeName; }
    public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public LocalDate getEstablishedDate() { return establishedDate; }
    public void setEstablishedDate(LocalDate establishedDate) { this.establishedDate = establishedDate; }
    public Long getAssignedSalesId() { return assignedSalesId; }
    public void setAssignedSalesId(Long assignedSalesId) { this.assignedSalesId = assignedSalesId; }
    public String getAssignedSalesEmployeeNo() { return assignedSalesEmployeeNo; }
    public void setAssignedSalesEmployeeNo(String assignedSalesEmployeeNo) { this.assignedSalesEmployeeNo = assignedSalesEmployeeNo; }
    public String getAssignedSalesName() { return assignedSalesName; }
    public void setAssignedSalesName(String assignedSalesName) { this.assignedSalesName = assignedSalesName; }
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
