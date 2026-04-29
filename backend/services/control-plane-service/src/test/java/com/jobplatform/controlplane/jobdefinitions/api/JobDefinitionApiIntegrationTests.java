package com.jobplatform.controlplane.jobdefinitions.api;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private ResultActions createJobDefinition(String name) throws Exception {
        CreatePayload payload = new CreatePayload(name, "HTTP_CALL", 5, 3, 60);

        return mockMvc.perform(post("/api/v1/job-definitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));
    }

    private String createJobDefinitionAndReturnId(String name) throws Exception {
        String responseBody = createJobDefinition(name)
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
