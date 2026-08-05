package com.workbridge.server.dto.integration;

import java.util.List;

public record InterfaceDetailResponse(InterfaceResponse summary, String requestMethod, String requestUrl,
    String requestHeaders, String requestBody, String responseHeaders, String responseBody, List<InterfaceAttemptResponse> attempts) {
}
