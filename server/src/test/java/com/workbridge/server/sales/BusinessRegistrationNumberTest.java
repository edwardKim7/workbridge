package com.workbridge.server.sales;

import com.workbridge.server.domain.sales.BusinessRegistrationNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessRegistrationNumberTest {

    @Test
    void normalizeRemovesFormattingCharactersAndFormatRestoresDisplayForm() {
        String normalized = BusinessRegistrationNumber.normalize(" 000-00-00001 ");

        assertThat(normalized).isEqualTo("0000000001");
        assertThat(BusinessRegistrationNumber.isValid(normalized)).isTrue();
        assertThat(BusinessRegistrationNumber.format(normalized)).isEqualTo("000-00-00001");
    }

    @Test
    void invalidLengthOrUnsupportedCharactersAreRejectedAndFormatterDoesNotFabricateAValue() {
        assertThat(BusinessRegistrationNumber.isValid("000-00-0001")).isFalse();
        assertThat(BusinessRegistrationNumber.isValid("ABC000-00-00001")).isFalse();
        assertThat(BusinessRegistrationNumber.hasSupportedCharacters("101 81 23456")).isTrue();
        assertThat(BusinessRegistrationNumber.hasSupportedCharacters("B2B 고객")).isFalse();
        assertThat(BusinessRegistrationNumber.format("000-00-0001")).isEqualTo("000-00-0001");
        assertThat(BusinessRegistrationNumber.format("ABC000-00-00001")).isEqualTo("ABC000-00-00001");
    }
}
