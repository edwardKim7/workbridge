package com.workbridge.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI workBridgeOpenApi(WorkBridgeProperties properties) {
        return new OpenAPI().info(new Info()
                .title("WorkBridge REST API")
                .version(properties.getVersion())
                .description("영업지원·전자결재 업무 API입니다. 외부 ERP는 독립된 Mock ERP 서비스로 재현합니다."));
    }
}
