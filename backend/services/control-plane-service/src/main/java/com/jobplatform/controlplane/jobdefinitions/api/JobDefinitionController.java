package com.jobplatform.controlplane.jobdefinitions.api;

import com.jobplatform.controlplane.jobdefinitions.application.CreateJobDefinitionCommand;
import com.jobplatform.controlplane.jobdefinitions.application.JobDefinitionSearchCriteria;
import com.jobplatform.controlplane.jobdefinitions.application.JobDefinitionService;
import com.jobplatform.controlplane.jobdefinitions.application.UpdateJobDefinitionCommand;
import com.jobplatform.controlplane.jobdefinitions.domain.JobDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-definitions")
@Validated
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

    @GetMapping
    public JobDefinitionListResponse list(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {
        JobDefinitionSearchCriteria criteria = new JobDefinitionSearchCriteria(
                enabled,
                jobType,
                name,
                limit,
                offset
        );

        List<JobDefinitionResponse> items = service.list(criteria).stream()
                .map(JobDefinitionResponse::from)
                .toList();

        return new JobDefinitionListResponse(items, limit, offset, service.count(criteria));
    }

    @PutMapping("/{id}")
    public JobDefinitionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobDefinitionRequest request
    ) {
        JobDefinition updated = service.update(id, new UpdateJobDefinitionCommand(
                request.name(),
                request.jobType(),
                request.priority(),
                request.maxRetries(),
                request.timeoutSeconds()
        ));

        return JobDefinitionResponse.from(updated);
    }

    @PatchMapping("/{id}/enable")
    public JobDefinitionResponse enable(@PathVariable UUID id) {
        return JobDefinitionResponse.from(service.enable(id));
    }

    @PatchMapping("/{id}/disable")
    public JobDefinitionResponse disable(@PathVariable UUID id) {
        return JobDefinitionResponse.from(service.disable(id));
    }
}
