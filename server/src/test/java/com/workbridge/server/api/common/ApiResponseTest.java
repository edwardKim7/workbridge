package com.workbridge.server.api.common;

import com.workbridge.server.audit.TraceIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void successResponseContainsDataMessageAndCurrentTraceId() {
        MDC.put(TraceIdContext.MDC_KEY, "trace-api-0001");

        ApiResponse<String> response = ApiResponse.success("connected", "연결되었습니다.");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("connected");
        assertThat(response.getMessage()).isEqualTo("연결되었습니다.");
        assertThat(response.getTraceId()).isEqualTo("trace-api-0001");
        assertThat(response.getTimestamp()).isNotNull();
    }
}
