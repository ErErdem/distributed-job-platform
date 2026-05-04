package com.jobplatform.controlplane.jobdefinitions.application;

public record UpdateJobDefinitionCommand(
        String name,
        String jobType,
        int priority,
        int maxRetries,
        int timeoutSeconds
) {
}
