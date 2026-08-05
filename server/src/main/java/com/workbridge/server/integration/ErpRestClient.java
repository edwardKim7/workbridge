package com.workbridge.server.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.config.ErpIntegrationProperties;
import com.workbridge.server.domain.integration.ErpSimulationMode;
import com.workbridge.server.dto.integration.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

@Component
public class ErpRestClient {
    private final ErpIntegrationProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient client;
    public ErpRestClient(ErpIntegrationProperties p, ObjectMapper mapper) {
        this.properties = p;
        this.mapper = mapper;
        this.client = HttpClient.newBuilder().connectTimeout(p.getConnectTimeout()).build();
    }
    public ErpHttpResult send(ErpSalesPayload payload, String idempotencyKey, ErpSimulationMode mode,
        String traceId) {
        try {
            String body = mapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getBaseUrl() + "/api/v1/erp/sales"))
                .timeout(properties.getReadTimeout()).header("Content-Type", "application/json").header("Accept",
                "application/json").header("Idempotency-Key", idempotencyKey).header("X-Trace-Id", traceId)
                .header("X-Mock-Mode", mode.name()).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status<300) return new ErpHttpResult(status, response.body(), mapper
                .readValue(response.body(), ErpSalesResult.class));
            boolean temporary = status >= 500 || status == 408 || status == 429;
            boolean retryable = status >= 500 || status == 429;
            throw new ErpCallException("ERP_HTTP_" + status, "Mock ERP HTTP " + status, temporary, retryable,
                status, response.body(), null);
        } catch (HttpTimeoutException e) {
            throw new ErpCallException("ERP_TIMEOUT", "Mock ERP 응답 시간이 초과되었습니다.", true, false, null,
                null, e);
        } catch (ErpCallException e) {
            throw e;
        } catch (IOException e) {
            throw new ErpCallException("ERP_IO_ERROR", "Mock ERP 연결에 실패했습니다.", true, false, null, null,
                e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErpCallException("ERP_INTERRUPTED", "ERP 전송 대기가 중단되었습니다.", true, false, null, null,
                e);
        }
    }
    public record ErpHttpResult(int statusCode, String rawBody, ErpSalesResult result) {
    }
}
