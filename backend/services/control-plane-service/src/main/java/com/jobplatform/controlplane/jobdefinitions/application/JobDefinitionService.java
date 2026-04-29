package com.jobplatform.controlplane.jobdefinitions.application;

import com.jobplatform.controlplane.jobdefinitions.domain.JobDefinition;
import com.jobplatform.controlplane.jobdefinitions.persistence.JobDefinitionRepository;
import com.jobplatform.controlplane.shared.error.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobDefinitionService {

    private final JobDefinitionRepository repository;

    public JobDefinitionService(JobDefinitionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JobDefinition create(CreateJobDefinitionCommand command) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        JobDefinition jobDefinition = new JobDefinition(
                UUID.randomUUID(),
                command.name(),
                command.jobType(),
                command.priority(),
                command.maxRetries(),
                command.timeoutSeconds(),
                true,
                now,
                now
        );

        return repository.save(jobDefinition);
    }

    @Transactional(readOnly = true)
    public JobDefinition getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job definition not found: " + id));
    }
}
