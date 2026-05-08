package com.jobplatform.controlplane.jobruns.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JobRun(
        UUID id,
        UUID jobDefinitionId,
        JobRunStatus status,
        int priority,
        String payloadJson,
        String idempotencyKey,
        OffsetDateTime createdAt,
        OffsetDateTime scheduledAt,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        int currentAttempt
) {
}
