package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class HeartbeatServiceTest {
    private final StationRepository stations = mock(StationRepository.class);
    private final StationHeartbeatRepository heartbeats = mock(StationHeartbeatRepository.class);
    private final EventService events = mock(EventService.class);
    private final HeartbeatService service = new HeartbeatService(stations, heartbeats, events);

    @Test
    void stationIsOfflineUntilRealHeartbeatAndRetryIsIdempotent() {
        Station station = new Station();
        ReflectionTestUtils.setField(station, "id", 7L);
        station.setLastSeen(Instant.now().minusSeconds(301));
        assertEquals("OFFLINE", station.getStatus());
        when(stations.findById(7L)).thenReturn(Optional.of(station));
        when(heartbeats.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        HeartbeatRequest request = new HeartbeatRequest("h1", new BigDecimal("40"), new BigDecimal("10"),
                new BigDecimal("50"));
        StationHeartbeat saved = service.record(7L, request);
        assertEquals("ONLINE", station.getStatus());
        when(heartbeats.findByMessageId("h1")).thenReturn(Optional.of(saved));
        assertSame(saved, service.record(7L, request));
        verify(heartbeats, times(1)).save(any());
        verify(events, times(1)).addEvent(any(), eq("ONLINE"), anyString());
    }

    @Test
    void retryAcceptsDatabaseNormalizedDecimalScale() {
        Station station = new Station();
        ReflectionTestUtils.setField(station, "id", 7L);
        when(stations.findById(7L)).thenReturn(Optional.of(station));
        StationHeartbeat stored = new StationHeartbeat();
        stored.setStation(station);
        stored.setCpuTemp(new BigDecimal("42.00"));
        stored.setFreeDiskGb(new BigDecimal("16.00"));
        stored.setMemoryPercent(new BigDecimal("38.00"));
        when(heartbeats.findByMessageId("same-heartbeat")).thenReturn(Optional.of(stored));

        HeartbeatRequest retry = new HeartbeatRequest("same-heartbeat", new BigDecimal("42.0"),
                new BigDecimal("16.0"), new BigDecimal("38.0"));
        assertSame(stored, service.record(7L, retry));
        verify(heartbeats, never()).save(any());
    }
}
