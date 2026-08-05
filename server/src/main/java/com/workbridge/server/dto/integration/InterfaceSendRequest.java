package com.workbridge.server.dto.integration;

import com.workbridge.server.domain.integration.ErpSimulationMode;

public record InterfaceSendRequest(ErpSimulationMode simulationMode) {
    public ErpSimulationMode effectiveMode() {
        return simulationMode == null ? ErpSimulationMode.NORMAL :
        simulationMode;
    }
}
