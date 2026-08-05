package com.workbridge.server.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingUtilsTest {

    @Test
    void masksSecretsAndPersonallyIdentifiableValues() {
        assertThat(MaskingUtils.maskRejectedValue("password", "PlainText!"))
                .isEqualTo("[MASKED]");
        assertThat(MaskingUtils.maskRejectedValue("accessToken", "token-value"))
                .isEqualTo("[MASKED]");
        assertThat(MaskingUtils.maskRejectedValue("contactEmail", "sales@workbridge.example"))
                .isEqualTo("s***@workbridge.example");
        assertThat(MaskingUtils.maskRejectedValue("contactPhone", "010-1234-5678"))
                .isEqualTo("***-****-5678");
        assertThat(MaskingUtils.maskRejectedValue("businessRegistrationNo", "1234567890"))
                .isEqualTo("123-**-67890");
    }

    @Test
    void truncatesUnexpectedLongRejectedValue() {
        String longValue = "A".repeat(120);

        assertThat(MaskingUtils.maskRejectedValue("description", longValue))
                .hasSize(103)
                .endsWith("...");
    }
}
