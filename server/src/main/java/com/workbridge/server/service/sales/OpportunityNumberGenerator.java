package com.workbridge.server.service.sales;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
public class OpportunityNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final Clock clock;

    public OpportunityNumberGenerator(Clock clock) {
        this.clock = clock;
    }

    public String next() {
        String date = LocalDate.now(clock).format(DATE_FORMAT);
        String suffix = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
        return "OPP-" + date + "-" + suffix;
    }
}
