package com.workbridge.server.domain.migration;

import java.time.LocalDate;

public class MigrationStagingRecord {
    private Long stagingId;
    private Long batchId;
    private int rowNo;
    private String rawData;
    private String normalizedData;
    private MigrationValidationStatus validationStatus;
    private MigrationOperationType operationType;
    private Long existingCustomerId;
    private Long assignedSalesId;
    private String businessRegistrationNo;
    private String customerName;
    private String representativeName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private LocalDate establishedDate;

    public Long getStagingId() {
        return stagingId;
    }

    public void setStagingId(Long v) {
        stagingId = v;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long v) {
        batchId = v;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int v) {
        rowNo = v;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String v) {
        rawData = v;
    }

    public String getNormalizedData() {
        return normalizedData;
    }

    public void setNormalizedData(String v) {
        normalizedData = v;
    }

    public MigrationValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(MigrationValidationStatus v) {
        validationStatus = v;
    }

    public MigrationOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(MigrationOperationType v) {
        operationType = v;
    }

    public Long getExistingCustomerId() {
        return existingCustomerId;
    }

    public void setExistingCustomerId(Long v) {
        existingCustomerId = v;
    }

    public Long getAssignedSalesId() {
        return assignedSalesId;
    }

    public void setAssignedSalesId(Long v) {
        assignedSalesId = v;
    }

    public String getBusinessRegistrationNo() {
        return businessRegistrationNo;
    }

    public void setBusinessRegistrationNo(String v) {
        businessRegistrationNo = v;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String v) {
        customerName = v;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String v) {
        representativeName = v;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String v) {
        contactName = v;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String v) {
        contactEmail = v;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String v) {
        contactPhone = v;
    }

    public LocalDate getEstablishedDate() {
        return establishedDate;
    }

    public void setEstablishedDate(LocalDate v) {
        establishedDate = v;
    }
}
