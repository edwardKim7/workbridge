package com.workbridge.server.service.system;

import com.workbridge.server.config.WorkBridgeProperties;
import com.workbridge.server.domain.common.ServiceHealth;
import com.workbridge.server.dto.system.SystemPingResponse;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import com.workbridge.server.repository.system.SystemStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class SystemStatusService {

    private final SystemStatusRepository systemStatusRepository;
    private final WorkBridgeProperties properties;
    private final Clock clock;

    public SystemStatusService(
            SystemStatusRepository systemStatusRepository,
            WorkBridgeProperties properties,
            Clock clock
    ) {
        this.systemStatusRepository = systemStatusRepository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SystemPingResponse getPingStatus() {
        if (!systemStatusRepository.isDatabaseAvailable()) {
            throw new BusinessException(ErrorCode.DATABASE_UNAVAILABLE);
        }

        return new SystemPingResponse(
                properties.getName(),
                properties.getVersion(),
                ServiceHealth.UP,
                ServiceHealth.UP,
                LocalDateTime.now(clock)
        );
    }
}
