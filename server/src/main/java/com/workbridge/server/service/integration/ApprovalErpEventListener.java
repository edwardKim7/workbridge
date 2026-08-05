package com.workbridge.server.service.integration;

import com.workbridge.server.service.approval.ApprovalCompletedEvent;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

@Component
public class ApprovalErpEventListener {
    private static final Logger log = LoggerFactory.getLogger(ApprovalErpEventListener.class);
    private final ErpIntegrationService service;

    public ApprovalErpEventListener(ErpIntegrationService s) {
        service = s;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ApprovalCompletedEvent e) {
        try {
            service.sendFromApproval(e.approvalId(), e.actorId());
        } catch (RuntimeException ex) {
            log.warn("Approved opportunity ERP interface was recorded as failed or could not start. approvalId={}", e.approvalId(), ex);
        }
    }
}
