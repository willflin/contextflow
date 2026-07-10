package com.contextflow.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void learnerEndpointShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/learner/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void learnerTokenShouldAccessLearnerEndpoint() throws Exception {
        mockMvc.perform(get("/api/learner/probe")
                        .header("Authorization", "Bearer mock-token-learner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("LEARNER"));
    }

    @Test
    void learnerTokenShouldNotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/probe")
                        .header("Authorization", "Bearer mock-token-learner"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenShouldAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/probe")
                        .header("Authorization", "Bearer mock-token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("ADMIN"));
    }
}
