package com.jobplatform.controlplane.jobdefinitions.application;

public record JobDefinitionSearchCriteria(
        Boolean enabled,
        String jobType,
        String name,
        int limit,
        int offset,
        String sortDirection
) {
}
