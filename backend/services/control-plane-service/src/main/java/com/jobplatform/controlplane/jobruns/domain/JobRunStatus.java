package com.jobplatform.controlplane.jobruns.domain;

public enum JobRunStatus {
    QUEUED,
    DISPATCHED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    DEAD_LETTERED,
    CANCELLED
}
