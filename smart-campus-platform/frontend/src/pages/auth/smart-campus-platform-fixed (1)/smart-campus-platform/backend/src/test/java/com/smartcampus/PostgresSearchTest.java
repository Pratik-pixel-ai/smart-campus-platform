package com.smartcampus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Run only against a disposable PostgreSQL database: schema is recreated. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "postgres-test"})
@TestPropertySource(properties = "app.seed.enabled=true")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
@WithMockUser(authorities = "ROLE_ADMIN")
class PostgresSearchTest {
    @Autowired MockMvc mvc;

    @Test void allDirectoriesAcceptMissingEmptyAndMixedCaseSearches() throws Exception {
        for (String resource : new String[]{"students", "faculty", "subjects", "classrooms"}) {
            for (String suffix : new String[]{"", "?search=", "?search=a", "?search=A", "?search=zzzz-no-match"}) {
                mvc.perform(get("/api/" + resource + suffix))
                        .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
            }
        }
    }
    @Test void departmentsCanBeListedAndRead() throws Exception {
        mvc.perform(get("/api/departments")).andExpect(status().isOk());
        mvc.perform(get("/api/departments/1")).andExpect(status().isOk());
    }
    @Test void filtersAndPaginationRemainUsable() throws Exception {
        for (String resource : new String[]{"students", "faculty", "subjects"}) {
            mvc.perform(get("/api/" + resource).param("departmentId", "1").param("size", "2"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        }
        mvc.perform(get("/api/students").param("semester", "7"))
                .andExpect(status().isOk());
    }

    @Test void registrationWorksOnPostgres() throws Exception {
        // The public department list feeds the registration dropdown.
        mvc.perform(get("/api/departments/public")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        String payload = "{\"fullName\":\"Pg Student\",\"email\":\"Pg.Student@Example.com\","
                + "\"password\":\"Password@123\",\"role\":\"ROLE_STUDENT\",\"departmentId\":1,"
                + "\"rollNumber\":\"PG-001\",\"semester\":7,\"division\":\"A\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(payload.replace("Pg.Student@Example.com", "PG.STUDENT@example.com")
                                .replace("PG-001", "PG-002")))
                .andExpect(status().isConflict());
    }
}
