package com.workbridge.server.migration;

import com.workbridge.server.domain.migration.MigrationValidationStatus;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMigrationValidatorTest {

    private final CustomerMigrationValidator validator = new CustomerMigrationValidator();

    @Test
    void validRowIsNormalized() {
        CustomerMigrationValidator.Validation result = validator.validate(
                2,
                List.of(
                        "123-45-67890",
                        "신규 가상고객",
                        "김대표",
                        "이담당",
                        "owner@example.test",
                        "02-1234-5678",
                        "2020-01-02",
                        "WB-1001"
                ),
                new HashSet<>()
        );

        assertThat(result.errors()).isEmpty();
        assertThat(result.staging().getBusinessRegistrationNo()).isEqualTo("1234567890");
        assertThat(result.staging().getValidationStatus()).isEqualTo(MigrationValidationStatus.VALID);
    }

    @Test
    void missingValueBadDateAndDuplicateBusinessNumberProduceRowErrors() {
        HashSet<String> seen = new HashSet<>();
        validator.validate(2, List.of("1234567890", "첫 고객", "", "", "", "", "2020-01-01", "WB-1001"), seen);

        CustomerMigrationValidator.Validation result = validator.validate(
                3,
                List.of("1234567890", "", "", "", "bad-email", "", "2026/08/05", ""),
                seen
        );

        assertThat(result.errors())
                .extracting(error -> error.getErrorCode())
                .contains("DUPLICATE_IN_FILE", "REQUIRED", "INVALID_EMAIL", "INVALID_DATE");
        assertThat(result.staging().getValidationStatus()).isEqualTo(MigrationValidationStatus.INVALID);
    }
}
