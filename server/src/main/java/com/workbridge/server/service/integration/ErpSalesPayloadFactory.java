package com.workbridge.server.service.integration;

import com.workbridge.server.domain.approval.ApprovalRecord;
import com.workbridge.server.domain.sales.*;
import com.workbridge.server.dto.integration.ErpSalesPayload;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ErpSalesPayloadFactory {
    public ErpSalesPayload create(ApprovalRecord a, SalesOpportunityRecord o, CustomerRecord c) {
        return new ErpSalesPayload(a.getApprovalId(), a.getApprovalNo(), o.getOpportunityId(), o.getOpportunityNo(),
            o.getOpportunityName(), c.getCustomerId(), c.getBusinessRegistrationNo(), c.getCustomerName(),
            o.getStage().name(), o.getExpectedRevenue(), o.getOwnerEmployeeNo(), o.getOwnerEmployeeName(),
            o.getVersion(), a.getProcessedAt() == null ? LocalDateTime.now() : a.getProcessedAt());
    }
}
