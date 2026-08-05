package com.workbridge.server.integration;

import com.workbridge.server.config.ErpIntegrationProperties;
import org.springframework.stereotype.Component;

@Component
public class IntegrationPayloadSanitizer {

    private final int payloadLogLimit;

    public IntegrationPayloadSanitizer(ErpIntegrationProperties properties) {
        this.payloadLogLimit = properties.getPayloadLogLimit();
    }

    public Sanitized sanitize(String value) {
        if (value == null) {
            return new Sanitized(null, false);
        }

        String masked = value
                .replaceAll(
                        "(\\\"businessRegistrationNo\\\"\\s*:\\s*\\\")[0-9]{10}(\\\")",
                        "$1***MASKED***$2")
                .replaceAll(
                        "(\\\"contactPhone\\\"\\s*:\\s*\\\")[^\\\"]+(\\\")",
                        "$1***MASKED***$2");

        if (masked.length() <= payloadLogLimit) {
            return new Sanitized(masked, false);
        }
        return new Sanitized(masked.substring(0, payloadLogLimit), true);
    }

    public record Sanitized(String value, boolean truncated) {}
}
