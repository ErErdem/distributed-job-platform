CREATE TABLE job_definitions (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    job_type VARCHAR(100) NOT NULL,
    priority SMALLINT NOT NULL,
    max_retries SMALLINT NOT NULL,
    timeout_seconds INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_job_definitions_name UNIQUE (name)
);
