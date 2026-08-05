package com.workbridge.server.integration;

import com.workbridge.server.domain.approval.ApprovalRecord;
import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.OpportunityStage;
import com.workbridge.server.domain.sales.SalesOpportunityRecord;
import com.workbridge.server.dto.integration.ErpSalesPayload;
import com.workbridge.server.service.integration.ErpSalesPayloadFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ErpSalesPayloadFactoryTest {

    @Test
    void approvedOpportunityIsConvertedToExternalContract() {
        ApprovalRecord approval = new ApprovalRecord();
        approval.setApprovalId(10L);
        approval.setApprovalNo("APR-TEST-001");
        approval.setProcessedAt(LocalDateTime.of(2026, 8, 5, 14, 30));

        SalesOpportunityRecord opportunity = new SalesOpportunityRecord();
        opportunity.setOpportunityId(20L);
        opportunity.setOpportunityNo("OPP-TEST-001");
        opportunity.setOpportunityName("가상 영업기회");
        opportunity.setCustomerId(30L);
        opportunity.setStage(OpportunityStage.NEGOTIATION);
        opportunity.setExpectedRevenue(new BigDecimal("55000000"));
        opportunity.setOwnerEmployeeNo("WB-1001");
        opportunity.setOwnerEmployeeName("김영업");
        opportunity.setVersion(4L);

        CustomerRecord customer = new CustomerRecord();
        customer.setCustomerId(30L);
        customer.setBusinessRegistrationNo("1234567890");
        customer.setCustomerName("가상고객");

        ErpSalesPayload payload = new ErpSalesPayloadFactory().create(approval, opportunity, customer);

        assertThat(payload.approvalNo()).isEqualTo("APR-TEST-001");
        assertThat(payload.opportunityNo()).isEqualTo("OPP-TEST-001");
        assertThat(payload.businessRegistrationNo()).isEqualTo("1234567890");
        assertThat(payload.confirmedRevenue()).isEqualByComparingTo("55000000");
        assertThat(payload.sourceVersion()).isEqualTo(4L);
    }
}
