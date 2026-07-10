package com.incidentanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentanalyzer.dto.auth.LoginRequest;
import com.incidentanalyzer.dto.auth.RegisterRequest;
import com.incidentanalyzer.dto.auth.TokenResponse;
import com.incidentanalyzer.dto.incident.IncidentCreateRequest;
import com.incidentanalyzer.model.IncidentPriority;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import com.incidentanalyzer.model.UserRole;
import com.incidentanalyzer.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class IncidentApiIntegrationTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.data.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("app.jwt.secret", () -> "integration-test-secret-with-sufficient-length-1234567890");
        registry.add("app.jwt.access-token-ttl", () -> Duration.ofMinutes(15).toString());
        registry.add("app.jwt.refresh-token-ttl", () -> Duration.ofDays(7).toString());
        registry.add("app.cors.allowed-origins[0]", () -> "http://localhost:8080");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerLoginCreateAndFetchIncident() throws Exception {
        String registerJson = objectMapper.writeValueAsString(new RegisterRequest("Engineer One", "engineer@example.com", "StrongPass123!", UserRole.ENGINEER));
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerJson))
                .andExpect(status().isOk());

        String loginJson = objectMapper.writeValueAsString(new LoginRequest("engineer@example.com", "StrongPass123!"));
        String loginResponse = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        TokenResponse tokens = objectMapper.readValue(loginResponse, TokenResponse.class);
        assertThat(tokens.accessToken()).isNotBlank();

        String incidentJson = objectMapper.writeValueAsString(new IncidentCreateRequest(
                "Database outage",
                "billing-service",
                "prod",
                IncidentSeverity.CRITICAL,
                IncidentPriority.URGENT,
                IncidentStatus.OPEN,
                "Connection refused",
                null,
                "stacktrace",
                "DB cluster offline",
                "Restarted primary node and failover completed",
                userRepository.findByEmail("engineer@example.com").orElseThrow().getId(),
                List.of("database", "billing"),
                List.of("postgres", "outage")));

        String created = mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .content(incidentJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String incidentId = objectMapper.readTree(created).get("incidentId").asText();
        String fetched = mockMvc.perform(get("/api/incidents/" + incidentId)
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(fetched).get("applicationName").asText()).isEqualTo("billing-service");
    }
}
