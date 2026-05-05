package com.jobplatform.controlplane.jobdefinitions.api;

import com.jobplatform.controlplane.shared.pagination.PageMetadata;
import java.util.List;

public record JobDefinitionListResponse(
        List<JobDefinitionResponse> items,
        PageMetadata page
) {
}
