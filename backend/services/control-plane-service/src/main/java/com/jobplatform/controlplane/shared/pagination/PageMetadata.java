package com.jobplatform.controlplane.shared.pagination;

public record PageMetadata(
        int limit,
        int offset,
        long total,
        boolean hasNext
) {
}
