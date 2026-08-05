package com.workbridge.mockerp;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockErpServiceTest {

    private final MockErpService service = new MockErpService();

    @Test
    void sameIdempotencyKeyReturnsExistingDocumentWithoutDuplicateCreation() {
        ErpSalesResponse first = service.process("KEY-1", request(), MockMode.NORMAL);
        ErpSalesResponse second = service.process("KEY-1", request(), MockMode.NORMAL);

        assertThat(first.duplicate()).isFalse();
        assertThat(second.duplicate()).isTrue();
        assertThat(second.documentNo()).isEqualTo(first.documentNo());
        assertThat(service.records()).hasSize(1);
    }

    @Test
    void intendedHttpAndValidationFailuresAreReproduced() {
        assertThatThrownBy(() -> service.process("KEY-500", request(), MockMode.HTTP_500))
                .isInstanceOfSatisfying(MockErpException.class,
                        exception -> assertThat(exception.status()).isEqualTo(500));
        assertThatThrownBy(() -> service.process("KEY-400", request(), MockMode.VALIDATION_400))
                .isInstanceOfSatisfying(MockErpException.class,
                        exception -> assertThat(exception.status()).isEqualTo(400));
        assertThat(service.records()).isEmpty();
    }

    private ErpSalesRequest request() {
        return new ErpSalesRequest(
                1L,
                "APR-1",
                1L,
                "OPP-1",
                "가상 영업기회",
                1L,
                "1234567890",
                "가상고객",
                "NEGOTIATION",
                new BigDecimal("1000000"),
                "WB-1001",
                "김영업",
                0L,
                LocalDateTime.of(2026, 8, 5, 12, 0)
        );
    }
}
