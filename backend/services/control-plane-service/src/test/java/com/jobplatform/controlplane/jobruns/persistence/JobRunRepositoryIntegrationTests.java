package com.jobplatform.controlplane.jobruns.persistence;

import static com.jobplatform.controlplane.jooq.generated.Tables.JOB_DEFINITIONS;
import static org.assertj.core.api.Assertions.assertThat;

import com.jobplatform.controlplane.jobruns.domain.JobRun;
import com.jobplatform.controlplane.jobruns.domain.JobRunStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JobRunRepositoryIntegrationTests {

    @Autowired
    private JobRunRepository repository;

    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void cleanDatabase() {
        dsl.deleteFrom(com.jobplatform.controlplane.jooq.generated.Tables.JOB_RUNS).execute();
        dsl.deleteFrom(JOB_DEFINITIONS).execute();
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        cleanDatabase();
    }

    @Test
    void saveAndFindByIdRoundTripsJobRunFields() {
        UUID jobDefinitionId = createJobDefinition();
        UUID jobRunId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 5, 6, 10, 15, 30, 0, ZoneOffset.UTC);
        OffsetDateTime scheduledAt = createdAt.plusMinutes(5);

        JobRun jobRun = new JobRun(
                jobRunId,
                jobDefinitionId,
                JobRunStatus.QUEUED,
                5,
                "{}",
                "run-key-001",
                createdAt,
                scheduledAt,
                null,
                null,
                0
        );

        JobRun saved = repository.save(jobRun);
        Optional<JobRun> found = repository.findById(jobRunId);

        assertJobRunMatches(saved, jobRun);
        assertThat(found).isPresent();
        assertJobRunMatches(found.get(), jobRun);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    private UUID createJobDefinition() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        dsl.insertInto(JOB_DEFINITIONS)
                .set(JOB_DEFINITIONS.ID, id)
                .set(JOB_DEFINITIONS.NAME, "email-report")
                .set(JOB_DEFINITIONS.JOB_TYPE, "HTTP_CALL")
                .set(JOB_DEFINITIONS.PRIORITY, (short) 5)
                .set(JOB_DEFINITIONS.MAX_RETRIES, (short) 3)
                .set(JOB_DEFINITIONS.TIMEOUT_SECONDS, 60)
                .set(JOB_DEFINITIONS.ENABLED, true)
                .set(JOB_DEFINITIONS.CREATED_AT, now)
                .set(JOB_DEFINITIONS.UPDATED_AT, now)
                .execute();

        return id;
    }

    private void assertJobRunMatches(JobRun actual, JobRun expected) {
        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.jobDefinitionId()).isEqualTo(expected.jobDefinitionId());
        assertThat(actual.status()).isEqualTo(expected.status());
        assertThat(actual.priority()).isEqualTo(expected.priority());
        assertThat(actual.payloadJson()).isEqualTo(expected.payloadJson());
        assertThat(actual.idempotencyKey()).isEqualTo(expected.idempotencyKey());
        assertThat(actual.createdAt().toInstant()).isEqualTo(expected.createdAt().toInstant());
        assertThat(actual.scheduledAt().toInstant()).isEqualTo(expected.scheduledAt().toInstant());
        assertThat(actual.startedAt()).isEqualTo(expected.startedAt());
        assertThat(actual.finishedAt()).isEqualTo(expected.finishedAt());
        assertThat(actual.currentAttempt()).isEqualTo(expected.currentAttempt());
    }
}
