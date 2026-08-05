package com.workbridge.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "workbridge.integration.erp")
public class ErpIntegrationProperties {
    private String baseUrl = "http://localhost:8081";
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(3);
    private int maxAttempts = 2;
    private Duration retryBackoff = Duration.ofMillis(250);
    private int payloadLogLimit = 8000;
    public String getBaseUrl(){return baseUrl;} public void setBaseUrl(String v){baseUrl=v;}
    public Duration getConnectTimeout(){return connectTimeout;} public void setConnectTimeout(Duration v){connectTimeout=v;}
    public Duration getReadTimeout(){return readTimeout;} public void setReadTimeout(Duration v){readTimeout=v;}
    public int getMaxAttempts(){return maxAttempts;} public void setMaxAttempts(int v){maxAttempts=Math.max(1,Math.min(v,3));}
    public Duration getRetryBackoff(){return retryBackoff;} public void setRetryBackoff(Duration v){retryBackoff=v;}
    public int getPayloadLogLimit(){return payloadLogLimit;} public void setPayloadLogLimit(int v){payloadLogLimit=Math.max(1000,v);}
}
