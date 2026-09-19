package com.smartcampus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks over the real filter chain: authentication, authorisation and the
 * two reads a reviewer is most likely to try first.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.seed.enabled=true")
class SmartCampusApiTest {

    private static final String PASSWORD = "Demo@1234";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginReturnsATokenForValidCredentials() throws Exception {
        String token = login("student@smartcampus.com");
        assertTrue(token != null && !token.isBlank());
    }

    @Test
    void loginIsRejectedForAWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@smartcampus.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRejectRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotReachAdminOnlyEndpoints() throws Exception {
        String token = login("student@smartcampus.com");
        // The access control matrix is enforced by the backend, not only by the UI.
        mockMvc.perform(get("/api/students").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanPageThroughStudents() throws Exception {
        String token = login("admin@smartcampus.com");
        String body = mockMvc.perform(get("/api/students?page=0&size=5&sort=rollNumber,asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode page = objectMapper.readTree(body);
        assertEquals(5, page.get("content").size());
        assertTrue(page.get("totalElements").asLong() >= 12);
    }

    @Test
    void studentCanReadTheirOwnAttendanceSummary() throws Exception {
        String token = login("student@smartcampus.com");
        String body = mockMvc.perform(get("/api/attendance/me/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode summary = objectMapper.readTree(body);
        assertTrue(summary.get("totalLectures").asLong() > 0);
        assertTrue(summary.get("overallPercentage").asDouble() > 0);
    }

    @Test
    void facultyCanCreateAnAssignment() throws Exception {
        String token = login("faculty@smartcampus.com");
        String payload = """
                {
                  "title": "ER modelling exercise",
                  "description": "Draw an ER diagram for a campus library and map it to tables.",
                  "subjectId": 1,
                  "deadline": "2030-01-15T23:59",
                  "maxMarks": 20,
                  "semester": 7,
                  "division": "A"
                }
                """;

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void assignmentValidationRejectsAnEmptyTitle() throws Exception {
        String token = login("faculty@smartcampus.com");
        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"x\",\"subjectId\":1,\"deadline\":\"2030-01-15T23:59\",\"maxMarks\":20,\"semester\":7,\"division\":\"A\"}"))
                .andExpect(status().isBadRequest());
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
