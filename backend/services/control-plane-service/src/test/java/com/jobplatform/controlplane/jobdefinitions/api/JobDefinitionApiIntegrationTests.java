package com.jobplatform.controlplane.jobdefinitions.api;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class JobDefinitionApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM job_definitions");
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        cleanDatabase();
    }

    @Test
    void postCreatesJobDefinition() throws Exception {
        createJobDefinition("email-report")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/job-definitions/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("email-report"))
                .andExpect(jsonPath("$.jobType").value("HTTP_CALL"))
                .andExpect(jsonPath("$.priority").value(5))
                .andExpect(jsonPath("$.maxRetries").value(3))
                .andExpect(jsonPath("$.timeoutSeconds").value(60))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getByIdReturnsCreatedJobDefinition() throws Exception {
        String id = createJobDefinitionAndReturnId("email-report");

        mockMvc.perform(get("/api/v1/job-definitions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("email-report"))
                .andExpect(jsonPath("$.jobType").value("HTTP_CALL"))
                .andExpect(jsonPath("$.priority").value(5))
                .andExpect(jsonPath("$.maxRetries").value(3))
                .andExpect(jsonPath("$.timeoutSeconds").value(60))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void postValidationFailureReturnsBadRequest() throws Exception {
        CreatePayload payload = new CreatePayload("", "", 11, -1, 0);

        mockMvc.perform(post("/api/v1/job-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions"));
    }

    @Test
    void getUnknownIdReturnsNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/job-definitions/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions/" + unknownId));
    }

    @Test
    void duplicateNameReturnsConflict() throws Exception {
        createJobDefinition("email-report")
                .andExpect(status().isCreated());

        createJobDefinition("email-report")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Job definition with name 'email-report' already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions"));
    }

    @Test
    void listReturnsPaginatedObjectResponse() throws Exception {
        createJobDefinition("email-report")
                .andExpect(status().isCreated());
        createJobDefinition("cleanup-temp-files")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/job-definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void listSupportsEnabledFilter() throws Exception {
        String enabledId = createJobDefinitionAndReturnId("enabled-job");
        String disabledId = createJobDefinitionAndReturnId("disabled-job");

        disableJobDefinition(disabledId)
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/job-definitions")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(disabledId))
                .andExpect(jsonPath("$.items[0].enabled").value(false))
                .andExpect(jsonPath("$.total").value(1));

        mockMvc.perform(get("/api/v1/job-definitions")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(enabledId))
                .andExpect(jsonPath("$.items[0].enabled").value(true))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void listSupportsJobTypeFilter() throws Exception {
        createJobDefinition("email-report", "HTTP_CALL")
                .andExpect(status().isCreated());
        createJobDefinition("daily-cleanup", "SHELL_COMMAND")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/job-definitions")
                        .param("jobType", "SHELL_COMMAND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("daily-cleanup"))
                .andExpect(jsonPath("$.items[0].jobType").value("SHELL_COMMAND"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void listSupportsCaseInsensitiveNameContainsFilter() throws Exception {
        createJobDefinition("Email-Report")
                .andExpect(status().isCreated());
        createJobDefinition("daily-cleanup")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/job-definitions")
                        .param("name", "mail-rep"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Email-Report"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void listValidatesLimitAndOffset() throws Exception {
        mockMvc.perform(get("/api/v1/job-definitions")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions"));

        mockMvc.perform(get("/api/v1/job-definitions")
                        .param("offset", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions"));
    }

    @Test
    void putUpdatesJobDefinition() throws Exception {
        String id = createJobDefinitionAndReturnId("email-report");
        CreatePayload payload = new CreatePayload("email-report-v2", "HTTP_CALL", 7, 4, 120);

        mockMvc.perform(put("/api/v1/job-definitions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("email-report-v2"))
                .andExpect(jsonPath("$.jobType").value("HTTP_CALL"))
                .andExpect(jsonPath("$.priority").value(7))
                .andExpect(jsonPath("$.maxRetries").value(4))
                .andExpect(jsonPath("$.timeoutSeconds").value(120))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void putUnknownIdReturnsNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        CreatePayload payload = new CreatePayload("email-report-v2", "HTTP_CALL", 7, 4, 120);

        mockMvc.perform(put("/api/v1/job-definitions/{id}", unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions/" + unknownId));
    }

    @Test
    void putDuplicateNameReturnsConflict() throws Exception {
        createJobDefinitionAndReturnId("email-report");
        String id = createJobDefinitionAndReturnId("cleanup-job");
        CreatePayload payload = new CreatePayload("email-report", "HTTP_CALL", 5, 3, 60);

        mockMvc.perform(put("/api/v1/job-definitions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Job definition with name 'email-report' already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions/" + id));
    }

    @Test
    void patchDisableChangesEnabledToFalse() throws Exception {
        String id = createJobDefinitionAndReturnId("email-report");

        disableJobDefinition(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void patchDisableIsIdempotent() throws Exception {
        String id = createJobDefinitionAndReturnId("email-report");

        disableJobDefinition(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        disableJobDefinition(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void patchEnableChangesEnabledToTrue() throws Exception {
        String id = createJobDefinitionAndReturnId("email-report");

        disableJobDefinition(id)
                .andExpect(status().isOk());

        enableJobDefinition(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void patchEnableIsIdempotent() throws Exception {
        String id = createJobDefinitionAndReturnId("email-report");

        enableJobDefinition(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        enableJobDefinition(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void patchEnableAndDisableUnknownIdReturnNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/job-definitions/{id}/enable", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions/" + unknownId + "/enable"));

        mockMvc.perform(patch("/api/v1/job-definitions/{id}/disable", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/job-definitions/" + unknownId + "/disable"));
    }

    private ResultActions createJobDefinition(String name) throws Exception {
        return createJobDefinition(name, "HTTP_CALL");
    }

    private ResultActions createJobDefinition(String name, String jobType) throws Exception {
        CreatePayload payload = new CreatePayload(name, jobType, 5, 3, 60);

        return mockMvc.perform(post("/api/v1/job-definitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));
    }

    private ResultActions disableJobDefinition(String id) throws Exception {
        return mockMvc.perform(patch("/api/v1/job-definitions/{id}/disable", id));
    }

    private ResultActions enableJobDefinition(String id) throws Exception {
        return mockMvc.perform(patch("/api/v1/job-definitions/{id}/enable", id));
    }

    private String createJobDefinitionAndReturnId(String name) throws Exception {
        return createJobDefinitionAndReturnId(name, "HTTP_CALL");
    }

    private String createJobDefinitionAndReturnId(String name, String jobType) throws Exception {
        String responseBody = createJobDefinition(name, jobType)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asText();
    }

    private record CreatePayload(
            String name,
            String jobType,
            Integer priority,
            Integer maxRetries,
            Integer timeoutSeconds
    ) {
    }
}
