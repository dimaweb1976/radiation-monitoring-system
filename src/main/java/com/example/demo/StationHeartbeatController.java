package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/heartbeat")
public class StationHeartbeatController {

    private final StationHeartbeatRepository heartbeatRepository;
    private final StationRepository stationRepository;
    private final EventService eventService;

    public StationHeartbeatController(
            StationHeartbeatRepository heartbeatRepository,
            StationRepository stationRepository,
            EventService eventService
    ) {
        this.heartbeatRepository = heartbeatRepository;
        this.stationRepository = stationRepository;
        this.eventService = eventService;
    }

    @GetMapping
    public List<StationHeartbeat> getAll() {
        return heartbeatRepository.findAll();
    }

    @GetMapping("/latest")
    public List<StationHeartbeat> latest() {
        return stationRepository.findAll()
                .stream()
                .map(station -> heartbeatRepository
                        .findTopByStationIdOrderByCreatedAtDesc(station.getId())
                        .orElse(null))
                .filter(h -> h != null)
                .toList();
    }

    @PostMapping
    public StationHeartbeat create(@RequestBody StationHeartbeat heartbeat) {
        Station station = stationRepository.findById(heartbeat.getStation().getId())
                .orElseThrow();

        station.setLastSeen(LocalDateTime.now());
        station.setStatus("ONLINE");
        stationRepository.save(station);

        heartbeat.setStation(station);
        heartbeat.setStatus("ONLINE");

        eventService.addEvent(station, "ONLINE", "Heartbeat received from station");

        if (heartbeat.getCpuTemp() != null &&
                heartbeat.getCpuTemp().doubleValue() >= 70.0) {
            eventService.addEvent(
                    station,
                    "CPU_HOT",
                    "CPU temperature is high: " + heartbeat.getCpuTemp() + " °C"
            );
        }

        if (heartbeat.getFreeDiskGb() != null &&
                heartbeat.getFreeDiskGb().doubleValue() <= 5.0) {
            eventService.addEvent(
                    station,
                    "LOW_DISK",
                    "Free disk space is low: " + heartbeat.getFreeDiskGb() + " GB"
            );
        }

        return heartbeatRepository.save(heartbeat);
    }
}
