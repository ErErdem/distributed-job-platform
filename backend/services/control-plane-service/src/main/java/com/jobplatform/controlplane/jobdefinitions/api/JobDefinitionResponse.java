package com.jobplatform.controlplane.jobdefinitions.api;

import com.jobplatform.controlplane.jobdefinitions.domain.JobDefinition;
import java.time.OffsetDateTime;
import java.util.UUID;

public record JobDefinitionResponse(
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

    public static JobDefinitionResponse from(JobDefinition jobDefinition) {
        return new JobDefinitionResponse(
                jobDefinition.id(),
                jobDefinition.name(),
                jobDefinition.jobType(),
                jobDefinition.priority(),
                jobDefinition.maxRetries(),
                jobDefinition.timeoutSeconds(),
                jobDefinition.enabled(),
                jobDefinition.createdAt(),
                jobDefinition.updatedAt()
        );
    }
}
