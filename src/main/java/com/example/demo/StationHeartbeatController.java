package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/heartbeat")
public class StationHeartbeatController {

    private final StationHeartbeatRepository heartbeatRepository;
    private final StationRepository stationRepository;
    private final HeartbeatService heartbeatService;

    public StationHeartbeatController(
            StationHeartbeatRepository heartbeatRepository,
            StationRepository stationRepository,
            HeartbeatService heartbeatService
    ) {
        this.heartbeatRepository = heartbeatRepository;
        this.stationRepository = stationRepository;
        this.heartbeatService = heartbeatService;
    }

    @GetMapping
    public List<StationHeartbeat> getAll() {
        return heartbeatRepository.findTop100ByOrderByCreatedAtDesc();
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
    public StationHeartbeat create(@RequestBody HeartbeatRequest request,
                                   @AuthenticationPrincipal DevicePrincipal device) {
        return heartbeatService.record(device.stationId(), request);
    }
}
