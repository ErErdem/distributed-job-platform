CREATE TABLE platform_metadata (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    description VARCHAR(255) NOT NULL
);

INSERT INTO platform_metadata (id, created_at, description)
VALUES (1, CURRENT_TIMESTAMP, 'Initial platform baseline migration');
