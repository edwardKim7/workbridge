package com.workbridge.server.dto.migration;

import java.util.List;

public record MigrationDetailResponse(
        MigrationBatchResponse batch,
        List<MigrationErrorResponse> errors) {}
