package com.jobplatform.controlplane.jobdefinitions.application;

public record CreateJobDefinitionCommand(
        String name,
        String jobType,
        int priority,
        int maxRetries,
        int timeoutSeconds
) {
}
