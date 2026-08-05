package com.workbridge.server.dto.sales;

import com.workbridge.server.domain.sales.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CustomerUpdateRequest {

    @NotBlank(message = "사업자번호는 필수입니다.")
    @Size(max = 20, message = "사업자번호 형식을 확인해 주세요.")
    private String businessRegistrationNo;

    @NotBlank(message = "고객명은 필수입니다.")
    @Size(max = 200, message = "고객명은 200자 이하여야 합니다.")
    private String customerName;

    @Size(max = 100, message = "대표자명은 100자 이하여야 합니다.")
    private String representativeName;

    @Size(max = 100, message = "담당자명은 100자 이하여야 합니다.")
    private String contactName;

    @Size(max = 30, message = "연락처는 30자 이하여야 합니다.")
    private String contactPhone;

    @Email(message = "이메일 형식을 확인해 주세요.")
    @Size(max = 200, message = "이메일은 200자 이하여야 합니다.")
    private String contactEmail;

    @PastOrPresent(message = "설립일은 오늘 이후일 수 없습니다.")
    private LocalDate establishedDate;

    @Positive(message = "담당 영업사원 ID는 1 이상이어야 합니다.")
    private Long assignedSalesId;

    @NotNull(message = "고객 상태는 필수입니다.")
    private CustomerStatus status;

    private boolean active = true;

    @NotNull(message = "버전은 필수입니다.")
    @PositiveOrZero(message = "버전은 0 이상이어야 합니다.")
    private Long version;

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
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
