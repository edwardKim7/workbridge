package com.workbridge.server.dto.integration;

public record ErpSalesResult(String documentNo, boolean duplicate, String message) {
}
