package com.jobplatform.controlplane.jobdefinitions.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateJobDefinitionRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 100)
        String jobType,

        @NotNull
        @Min(0)
        @Max(10)
        Integer priority,

        @NotNull
        @Min(0)
        @Max(10)
        Integer maxRetries,

        @NotNull
        @Min(1)
        @Max(86400)
        Integer timeoutSeconds
) {
}
