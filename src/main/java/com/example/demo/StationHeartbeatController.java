package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/heartbeat")
public class StationHeartbeatController {

    private final StationHeartbeatRepository heartbeatRepository;
    private final StationRepository stationRepository;

    public StationHeartbeatController(
            StationHeartbeatRepository heartbeatRepository,
            StationRepository stationRepository
    ) {
        this.heartbeatRepository = heartbeatRepository;
        this.stationRepository = stationRepository;
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

        return heartbeatRepository.save(heartbeat);
    }
}
