package com.jobplatform.controlplane.jobdefinitions.api;

import com.jobplatform.controlplane.jobdefinitions.application.CreateJobDefinitionCommand;
import com.jobplatform.controlplane.jobdefinitions.application.JobDefinitionService;
import com.jobplatform.controlplane.jobdefinitions.domain.JobDefinition;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-definitions")
public class JobDefinitionController {

    private final JobDefinitionService service;

    public JobDefinitionController(JobDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JobDefinitionResponse> create(@Valid @RequestBody CreateJobDefinitionRequest request) {
        JobDefinition created = service.create(new CreateJobDefinitionCommand(
                request.name(),
                request.jobType(),
                request.priority(),
                request.maxRetries(),
                request.timeoutSeconds()
        ));

        return ResponseEntity
                .created(URI.create("/api/v1/job-definitions/" + created.id()))
                .body(JobDefinitionResponse.from(created));
    }

    @GetMapping("/{id}")
    public JobDefinitionResponse getById(@PathVariable UUID id) {
        return JobDefinitionResponse.from(service.getById(id));
    }
}
