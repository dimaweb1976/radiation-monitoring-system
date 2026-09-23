package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeasurementController.class)
@Import(SecurityConfig.class)
@org.springframework.test.context.TestPropertySource(properties =
        "radiation.auth.device-tokens=7:long-device-token-1234")
class MeasurementApiSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean MeasurementRepository repository;
    @MockitoBean MeasurementService service;

    private static final String BODY = """
            {"detectorId":3,"value":0.546,"unit":"uSv/h",
             "messageId":"m1","measuredAt":"2026-09-23T12:00:00Z"}
            """;

    @Test
    void writeRequiresDeviceToken() throws Exception {
        mvc.perform(post("/api/measurements").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void deviceTokenProvidesBoundStationId() throws Exception {
        when(service.record(eq(3L), eq(new BigDecimal("0.546")), eq("uSv/h"), eq("m1"),
                eq(Instant.parse("2026-09-23T12:00:00Z")), eq(7L))).thenReturn(new Measurement());
        mvc.perform(post("/api/measurements").header("Authorization", "Bearer long-device-token-1234")
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isOk());
        verify(service).record(eq(3L), any(), eq("uSv/h"), eq("m1"), any(), eq(7L));
    }
}
