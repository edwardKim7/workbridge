package com.workbridge.server.sales;

import com.workbridge.server.service.sales.OpportunityNumberGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class OpportunityNumberGeneratorTest {

    @Test
    void generatedNumberContainsBusinessDateAndRandomSuffix() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        OpportunityNumberGenerator generator = new OpportunityNumberGenerator(clock);

        String first = generator.next();
        String second = generator.next();

        assertThat(first).matches("OPP-20260805-[0-9A-F]{12}");
        assertThat(second).matches("OPP-20260805-[0-9A-F]{12}");
        assertThat(second).isNotEqualTo(first);
    }
}
