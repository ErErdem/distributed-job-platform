package com.jobplatform.controlplane.jobdefinitions.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JobDefinition(
        UUID id,
        String name,
        String jobType,
        int priority,
        int maxRetries,
        int timeoutSeconds,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
