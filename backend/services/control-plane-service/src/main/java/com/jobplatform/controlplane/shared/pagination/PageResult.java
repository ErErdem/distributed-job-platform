package com.jobplatform.controlplane.shared.pagination;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        PageMetadata page
) {
}
