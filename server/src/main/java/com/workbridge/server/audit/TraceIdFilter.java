package com.workbridge.server.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Pattern SAFE_TRACE_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{7,63}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TraceIdContext.HEADER_NAME));
        MDC.put(TraceIdContext.MDC_KEY, traceId);
        request.setAttribute(TraceIdContext.REQUEST_ATTRIBUTE, traceId);
        response.setHeader(TraceIdContext.HEADER_NAME, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TraceIdContext.MDC_KEY);
        }
    }

    private String resolveTraceId(String requestedTraceId) {
        if (requestedTraceId != null && SAFE_TRACE_ID.matcher(requestedTraceId).matches()) {
            return requestedTraceId;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
