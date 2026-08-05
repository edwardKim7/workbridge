package com.workbridge.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "workbridge.migration")
public class MigrationProperties {
    private DataSize maxFileSize = DataSize.ofMegabytes(5);
    private int chunkSize = 500;
    public DataSize getMaxFileSize() {
        return maxFileSize;
    }
    public void setMaxFileSize(DataSize v) {
        maxFileSize = v;
    }
    public int getChunkSize() {
        return chunkSize;
    }
    public void setChunkSize(int v) {
        chunkSize = Math.max(50, Math.min(v, 1000));
    }
}
