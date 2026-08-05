package com.workbridge.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.api.common.ApiErrorResponse;
import com.workbridge.server.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException {
        write(request, response, errorCode, errorCode.getDefaultMessage());
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode errorCode,
            String message
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of(
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                message,
                Collections.emptyList(),
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}
