package com.workbridge.server.dto.system;

import com.workbridge.server.domain.common.ServiceHealth;

import java.time.LocalDateTime;

public class SystemPingResponse {

    private final String application;
    private final String version;
    private final ServiceHealth status;
    private final ServiceHealth database;
    private final LocalDateTime timestamp;

    public SystemPingResponse(
            String application,
            String version,
            ServiceHealth status,
            ServiceHealth database,
            LocalDateTime timestamp
    ) {
        this.application = application;
        this.version = version;
        this.status = status;
        this.database = database;
        this.timestamp = timestamp;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }

    public ServiceHealth getStatus() {
        return status;
    }

    public ServiceHealth getDatabase() {
        return database;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
