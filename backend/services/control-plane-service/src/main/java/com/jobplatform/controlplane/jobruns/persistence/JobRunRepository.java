package com.jobplatform.controlplane.jobruns.persistence;

import static com.jobplatform.controlplane.jooq.generated.Tables.JOB_RUNS;

import com.jobplatform.controlplane.jobruns.domain.JobRun;
import com.jobplatform.controlplane.jobruns.domain.JobRunStatus;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
public class JobRunRepository {

    private final DSLContext dsl;

    public JobRunRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public JobRun save(JobRun jobRun) {
        return mapRecord(dsl.insertInto(JOB_RUNS)
                .set(JOB_RUNS.ID, jobRun.id())
                .set(JOB_RUNS.JOB_DEFINITION_ID, jobRun.jobDefinitionId())
                .set(JOB_RUNS.STATUS, jobRun.status().name())
                .set(JOB_RUNS.PRIORITY, toSmallInt(jobRun.priority()))
                .set(JOB_RUNS.PAYLOAD, JSONB.valueOf(jobRun.payloadJson()))
                .set(JOB_RUNS.IDEMPOTENCY_KEY, jobRun.idempotencyKey())
                .set(JOB_RUNS.CREATED_AT, jobRun.createdAt())
                .set(JOB_RUNS.SCHEDULED_AT, jobRun.scheduledAt())
                .set(JOB_RUNS.STARTED_AT, jobRun.startedAt())
                .set(JOB_RUNS.FINISHED_AT, jobRun.finishedAt())
                .set(JOB_RUNS.CURRENT_ATTEMPT, toSmallInt(jobRun.currentAttempt()))
                .returning(
                        JOB_RUNS.ID,
                        JOB_RUNS.JOB_DEFINITION_ID,
                        JOB_RUNS.STATUS,
                        JOB_RUNS.PRIORITY,
                        JOB_RUNS.PAYLOAD,
                        JOB_RUNS.IDEMPOTENCY_KEY,
                        JOB_RUNS.CREATED_AT,
                        JOB_RUNS.SCHEDULED_AT,
                        JOB_RUNS.STARTED_AT,
                        JOB_RUNS.FINISHED_AT,
                        JOB_RUNS.CURRENT_ATTEMPT
                )
                .fetchSingle());
    }

    public Optional<JobRun> findById(UUID id) {
        return dsl.select(
                        JOB_RUNS.ID,
                        JOB_RUNS.JOB_DEFINITION_ID,
                        JOB_RUNS.STATUS,
                        JOB_RUNS.PRIORITY,
                        JOB_RUNS.PAYLOAD,
                        JOB_RUNS.IDEMPOTENCY_KEY,
                        JOB_RUNS.CREATED_AT,
                        JOB_RUNS.SCHEDULED_AT,
                        JOB_RUNS.STARTED_AT,
                        JOB_RUNS.FINISHED_AT,
                        JOB_RUNS.CURRENT_ATTEMPT
                )
                .from(JOB_RUNS)
                .where(JOB_RUNS.ID.eq(id))
                .fetchOptional(this::mapRecord);
    }

    private JobRun mapRecord(Record record) {
        return new JobRun(
                record.get(JOB_RUNS.ID),
                record.get(JOB_RUNS.JOB_DEFINITION_ID),
                JobRunStatus.valueOf(record.get(JOB_RUNS.STATUS)),
                record.get(JOB_RUNS.PRIORITY).intValue(),
                record.get(JOB_RUNS.PAYLOAD).data(),
                record.get(JOB_RUNS.IDEMPOTENCY_KEY),
                record.get(JOB_RUNS.CREATED_AT),
                record.get(JOB_RUNS.SCHEDULED_AT),
                record.get(JOB_RUNS.STARTED_AT),
                record.get(JOB_RUNS.FINISHED_AT),
                record.get(JOB_RUNS.CURRENT_ATTEMPT).intValue()
        );
    }

    private short toSmallInt(int value) {
        return (short) value;
    }
}
