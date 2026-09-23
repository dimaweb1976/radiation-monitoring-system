package com.example.demo;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HeartbeatService {
    private final StationRepository stations;
    private final StationHeartbeatRepository heartbeats;
    private final EventService events;

    public HeartbeatService(StationRepository stations, StationHeartbeatRepository heartbeats, EventService events) {
        this.stations = stations;
        this.heartbeats = heartbeats;
        this.events = events;
    }

    @Transactional
    public StationHeartbeat record(Long stationId, HeartbeatRequest request) {
        if (request == null || request.messageId() == null || request.messageId().isBlank()
                || request.messageId().length() > 100 || !inRange(request.cpuTemp(), 0, 150)
                || !inRange(request.freeDiskGb(), 0, 100000)
                || !inRange(request.memoryPercent(), 0, 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid heartbeat");
        }
        Station station = stations.findById(stationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));
        var existing = heartbeats.findByMessageId(request.messageId());
        if (existing.isPresent()) {
            StationHeartbeat old = existing.get();
            if (!old.getStation().getId().equals(stationId)
                    || !sameNumber(old.getCpuTemp(), request.cpuTemp())
                    || !sameNumber(old.getFreeDiskGb(), request.freeDiskGb())
                    || !sameNumber(old.getMemoryPercent(), request.memoryPercent())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Message ID reused with different data");
            }
            return old;
        }
        boolean wasOnline = "ONLINE".equals(station.getStatus());
        StationHeartbeat previous = heartbeats.findTopByStationIdOrderByCreatedAtDesc(stationId).orElse(null);
        station.setLastSeen(Instant.now());
        stations.save(station);
        StationHeartbeat heartbeat = new StationHeartbeat();
        heartbeat.setStation(station);
        heartbeat.setStatus("ONLINE");
        heartbeat.setMessageId(request.messageId());
        heartbeat.setCpuTemp(request.cpuTemp());
        heartbeat.setFreeDiskGb(request.freeDiskGb());
        heartbeat.setMemoryPercent(request.memoryPercent());
        StationHeartbeat saved = heartbeats.save(heartbeat);
        if (!wasOnline) events.addEvent(station, "ONLINE", "Heartbeat received from station");
        if (request.cpuTemp() != null && request.cpuTemp().compareTo(new BigDecimal("70")) >= 0
                && (previous == null || previous.getCpuTemp() == null
                || previous.getCpuTemp().compareTo(new BigDecimal("70")) < 0))
            events.addEvent(station, "CPU_HOT", "CPU temperature is high: " + request.cpuTemp() + " °C");
        if (request.freeDiskGb() != null && request.freeDiskGb().compareTo(new BigDecimal("5")) <= 0
                && (previous == null || previous.getFreeDiskGb() == null
                || previous.getFreeDiskGb().compareTo(new BigDecimal("5")) > 0))
            events.addEvent(station, "LOW_DISK", "Free disk space is low: " + request.freeDiskGb() + " GB");
        return saved;
    }

    private boolean inRange(BigDecimal value, int min, int max) {
        return value == null || (value.compareTo(BigDecimal.valueOf(min)) >= 0
                && value.compareTo(BigDecimal.valueOf(max)) <= 0);
    }

    private boolean sameNumber(BigDecimal stored, BigDecimal received) {
        if (stored == null || received == null) return stored == received;
        return stored.compareTo(received) == 0;
    }
}
