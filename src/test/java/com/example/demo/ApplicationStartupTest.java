package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:startup;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "radiation.auth.admin-token=long-admin-token-for-test",
        "radiation.auth.device-tokens=1:long-device-token-for-test"
})
@AutoConfigureMockMvc
class ApplicationStartupTest {
    @Autowired ApplicationContext context;
    @Autowired MockMvc mvc;

    @Test
    void realServerDoesNotStartDemoSimulators() {
        assertFalse(context.containsBean("heartbeatSimulator"));
        assertFalse(context.containsBean("sensorSimulator"));
    }

    @Test
    void fullApplicationAcceptsConfiguredTokens() throws Exception {
        mvc.perform(post("/api/stations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/stations").header("Authorization", "Bearer long-admin-token-for-test")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/measurements").header("Authorization", "Bearer long-device-token-for-test")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
