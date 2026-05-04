package com.jobplatform.controlplane.jobdefinitions.api;

import java.util.List;

public record JobDefinitionListResponse(
        List<JobDefinitionResponse> items,
        int limit,
        int offset,
        long total
) {
}
