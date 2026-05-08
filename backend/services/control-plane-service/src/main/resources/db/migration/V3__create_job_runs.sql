CREATE TABLE job_runs (
    id UUID PRIMARY KEY,
    job_definition_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    priority SMALLINT NOT NULL,
    payload JSONB NOT NULL,
    idempotency_key VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL,
    scheduled_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    current_attempt SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_job_runs_job_definition
        FOREIGN KEY (job_definition_id) REFERENCES job_definitions(id),
    CONSTRAINT chk_job_runs_status CHECK (
        status IN (
            'QUEUED',
            'DISPATCHED',
            'RUNNING',
            'SUCCEEDED',
            'FAILED',
            'DEAD_LETTERED',
            'CANCELLED'
        )
    ),
    CONSTRAINT chk_job_runs_current_attempt CHECK (current_attempt >= 0),
    CONSTRAINT chk_job_runs_priority CHECK (priority BETWEEN 0 AND 10),
    CONSTRAINT uq_job_runs_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_job_runs_status ON job_runs (status);
CREATE INDEX idx_job_runs_job_definition_id ON job_runs (job_definition_id);
CREATE INDEX idx_job_runs_created_at_desc ON job_runs (created_at DESC);
