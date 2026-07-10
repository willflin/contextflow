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

    private String loginToken(String username, String password) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(response, "$.data.token");
    }

    @Test
    void learnerEndpointShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/learner/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void learnerTokenShouldAccessLearnerEndpoint() throws Exception {
        String token = loginToken("learner", "learner123");

        mockMvc.perform(get("/api/learner/probe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("LEARNER"));
    }

    @Test
    void learnerTokenShouldNotAccessAdminEndpoint() throws Exception {
        String token = loginToken("learner", "learner123");

        mockMvc.perform(get("/api/admin/probe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenShouldAccessAdminEndpoint() throws Exception {
        String token = loginToken("admin", "admin123");

        mockMvc.perform(get("/api/admin/probe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("ADMIN"));
    }
}
