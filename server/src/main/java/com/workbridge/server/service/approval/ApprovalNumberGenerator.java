package com.workbridge.server.service.approval;

import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ApprovalNumberGenerator {
    private final Clock clock;
    public ApprovalNumberGenerator(Clock clock) {
        this.clock = clock;
    }
    public String next() {
        return "APR-" + LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + UUID.randomUUID()
            .toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
