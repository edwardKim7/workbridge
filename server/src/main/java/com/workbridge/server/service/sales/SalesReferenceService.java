package com.workbridge.server.service.sales;

import com.workbridge.server.dto.sales.SalesEmployeeOptionResponse;
import com.workbridge.server.repository.sales.SalesEmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesReferenceService {

    private final SalesEmployeeRepository salesEmployeeRepository;

    public SalesReferenceService(SalesEmployeeRepository salesEmployeeRepository) {
        this.salesEmployeeRepository = salesEmployeeRepository;
    }

    @Transactional(readOnly = true)
    public List<SalesEmployeeOptionResponse> getActiveSalesEmployees() {
        return salesEmployeeRepository.findActiveSalesEmployees().stream()
                .map(SalesEmployeeOptionResponse::new)
                .toList();
    }
}
