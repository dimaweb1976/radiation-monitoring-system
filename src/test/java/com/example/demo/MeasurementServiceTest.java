package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class MeasurementServiceTest {
    private final MeasurementRepository measurements = mock(MeasurementRepository.class);
    private final DetectorRepository detectors = mock(DetectorRepository.class);
    private final EventService events = mock(EventService.class);
    private final MeasurementService service = new MeasurementService(measurements, detectors, events,
            new BigDecimal("0.5"), new BigDecimal("1.0"));
    private final Instant measuredAt = Instant.parse("2026-09-23T12:00:00Z");

    @BeforeEach
    void setUp() {
        Station station = new Station();
        ReflectionTestUtils.setField(station, "id", 7L);
        Detector detector = new Detector();
        ReflectionTestUtils.setField(detector, "id", 3L);
        detector.setName("Gamma");
        detector.setStation(station);
        when(detectors.findById(3L)).thenReturn(Optional.of(detector));
        when(measurements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void warningIsCreatedForEverySourceUsingTheService() {
        Measurement saved = service.record(3L, new BigDecimal("0.546"), "uSv/h", "m1", measuredAt, 7L);
        assertEquals("m1", saved.getMessageId());
        verify(events).addEvent(any(), eq("RADIATION_WARNING"), anyString());
    }

    @Test
    void retryDoesNotSaveOrCreateAnotherEvent() {
        Measurement first = service.record(3L, new BigDecimal("1.1"), "uSv/h", "m1", measuredAt, 7L);
        when(measurements.findByMessageId("m1")).thenReturn(Optional.of(first));
        assertSame(first, service.record(3L, new BigDecimal("1.1"), "uSv/h", "m1", measuredAt, 7L));
        verify(measurements, times(1)).save(any());
        verify(events, times(1)).addEvent(any(), eq("RADIATION_ALARM"), anyString());
    }

    @Test
    void otherStationCannotSubmitMeasurement() {
        assertThrows(ResponseStatusException.class,
                () -> service.record(3L, BigDecimal.ONE, "uSv/h", "m1", measuredAt, 8L));
        verify(measurements, never()).save(any());
    }

    @Test
    void sustainedWarningDoesNotCreateDuplicateEvents() {
        Measurement previous = new Measurement();
        previous.setValue(new BigDecimal("0.6"));
        when(measurements.findTopByDetectorIdOrderByIdDesc(3L)).thenReturn(Optional.of(previous));
        service.record(3L, new BigDecimal("0.7"), "uSv/h", "m2", measuredAt, 7L);
        verifyNoInteractions(events);
    }
}
