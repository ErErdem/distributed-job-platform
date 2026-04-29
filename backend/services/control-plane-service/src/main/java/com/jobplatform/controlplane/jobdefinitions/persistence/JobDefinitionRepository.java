package com.jobplatform.controlplane.jobdefinitions.persistence;

import static com.jobplatform.controlplane.jooq.generated.Tables.JOB_DEFINITIONS;

import com.jobplatform.controlplane.jobdefinitions.domain.JobDefinition;
import com.jobplatform.controlplane.shared.error.DuplicateResourceException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class JobDefinitionRepository {

    private static final String POSTGRES_UNIQUE_VIOLATION = "23505";
    private static final String JOB_DEFINITIONS_NAME_CONSTRAINT = "uq_job_definitions_name";

    private final DSLContext dsl;

    public JobDefinitionRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public JobDefinition save(JobDefinition jobDefinition) {
        try {
            return mapRecord(dsl.insertInto(JOB_DEFINITIONS)
                    .set(JOB_DEFINITIONS.ID, jobDefinition.id())
                    .set(JOB_DEFINITIONS.NAME, jobDefinition.name())
                    .set(JOB_DEFINITIONS.JOB_TYPE, jobDefinition.jobType())
                    .set(JOB_DEFINITIONS.PRIORITY, toSmallInt(jobDefinition.priority()))
                    .set(JOB_DEFINITIONS.MAX_RETRIES, toSmallInt(jobDefinition.maxRetries()))
                    .set(JOB_DEFINITIONS.TIMEOUT_SECONDS, jobDefinition.timeoutSeconds())
                    .set(JOB_DEFINITIONS.ENABLED, jobDefinition.enabled())
                    .set(JOB_DEFINITIONS.CREATED_AT, jobDefinition.createdAt())
                    .set(JOB_DEFINITIONS.UPDATED_AT, jobDefinition.updatedAt())
                    .returning(
                            JOB_DEFINITIONS.ID,
                            JOB_DEFINITIONS.NAME,
                            JOB_DEFINITIONS.JOB_TYPE,
                            JOB_DEFINITIONS.PRIORITY,
                            JOB_DEFINITIONS.MAX_RETRIES,
                            JOB_DEFINITIONS.TIMEOUT_SECONDS,
                            JOB_DEFINITIONS.ENABLED,
                            JOB_DEFINITIONS.CREATED_AT,
                            JOB_DEFINITIONS.UPDATED_AT
                    )
                    .fetchSingle());
        } catch (DataIntegrityViolationException ex) {
            throwDuplicateNameIfMatched(jobDefinition.name(), ex);
            throw ex;
        } catch (DataAccessException ex) {
            throwDuplicateNameIfMatched(jobDefinition.name(), ex);
            throw ex;
        }
    }

    public Optional<JobDefinition> findById(UUID id) {
        return dsl.select(
                        JOB_DEFINITIONS.ID,
                        JOB_DEFINITIONS.NAME,
                        JOB_DEFINITIONS.JOB_TYPE,
                        JOB_DEFINITIONS.PRIORITY,
                        JOB_DEFINITIONS.MAX_RETRIES,
                        JOB_DEFINITIONS.TIMEOUT_SECONDS,
                        JOB_DEFINITIONS.ENABLED,
                        JOB_DEFINITIONS.CREATED_AT,
                        JOB_DEFINITIONS.UPDATED_AT
                )
                .from(JOB_DEFINITIONS)
                .where(JOB_DEFINITIONS.ID.eq(id))
                .fetchOptional(this::mapRecord);
    }

    private JobDefinition mapRecord(Record record) {
        return new JobDefinition(
                record.get(JOB_DEFINITIONS.ID),
                record.get(JOB_DEFINITIONS.NAME),
                record.get(JOB_DEFINITIONS.JOB_TYPE),
                record.get(JOB_DEFINITIONS.PRIORITY).intValue(),
                record.get(JOB_DEFINITIONS.MAX_RETRIES).intValue(),
                record.get(JOB_DEFINITIONS.TIMEOUT_SECONDS),
                record.get(JOB_DEFINITIONS.ENABLED),
                record.get(JOB_DEFINITIONS.CREATED_AT),
                record.get(JOB_DEFINITIONS.UPDATED_AT)
        );
    }

    private short toSmallInt(int value) {
        return (short) value;
    }

    private void throwDuplicateNameIfMatched(String name, RuntimeException exception) {
        if (isDuplicateNameViolation(exception)) {
            throw new DuplicateResourceException("Job definition with name '" + name + "' already exists", exception);
        }
    }

    private boolean isDuplicateNameViolation(Throwable exception) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof SQLException sqlException
                    && POSTGRES_UNIQUE_VIOLATION.equals(sqlException.getSQLState())
                    && containsConstraintName(sqlException)) {
                return true;
            }

            if (containsConstraintName(current)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsConstraintName(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && message.contains(JOB_DEFINITIONS_NAME_CONSTRAINT);
    }
}
