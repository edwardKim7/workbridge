package com.workbridge.server.audit;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void keepsSafeClientTraceIdAndAddsItToResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/system/ping");
        request.addHeader(TraceIdContext.HEADER_NAME, "client-trace-0001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(TraceIdContext.HEADER_NAME)).isEqualTo("client-trace-0001");
        assertThat(request.getAttribute(TraceIdContext.REQUEST_ATTRIBUTE)).isEqualTo("client-trace-0001");
        assertThat(MDC.get(TraceIdContext.MDC_KEY)).isNull();
    }

    @Test
    void replacesUnsafeTraceIdWithGeneratedIdentifier() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.addHeader(TraceIdContext.HEADER_NAME, "bad trace id<script>");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(TraceIdContext.HEADER_NAME))
                .matches("[a-f0-9]{32}")
                .isNotEqualTo("bad trace id<script>");
    }
}
