package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class HeartbeatSimulator {

    private final StationRepository stationRepository;
    private final StationHeartbeatRepository heartbeatRepository;
    private final EventService eventService;
    private final Random random = new Random();

    public HeartbeatSimulator(
            StationRepository stationRepository,
            StationHeartbeatRepository heartbeatRepository,
            EventService eventService
    ) {
        this.stationRepository = stationRepository;
        this.heartbeatRepository = heartbeatRepository;
        this.eventService = eventService;
    }

    @Scheduled(fixedRate = 30000)
    public void generateHeartbeat() {
        List<Station> stations = stationRepository.findAll();

        if (stations.isEmpty()) {
            System.out.println("HEARTBEAT SIMULATOR: no stations found");
            return;
        }

        for (Station station : stations) {
            StationHeartbeat heartbeat = new StationHeartbeat();

            heartbeat.setStation(station);
            heartbeat.setStatus("ONLINE");
            heartbeat.setCreatedAt(LocalDateTime.now());

            double cpu = 40 + random.nextDouble() * 25;
            double disk = 10 + random.nextDouble() * 20;
            double ram = 30 + random.nextDouble() * 40;

            heartbeat.setCpuTemp(BigDecimal.valueOf(cpu).setScale(2, java.math.RoundingMode.HALF_UP));
            heartbeat.setFreeDiskGb(BigDecimal.valueOf(disk).setScale(2, java.math.RoundingMode.HALF_UP));
            heartbeat.setMemoryPercent(BigDecimal.valueOf(ram).setScale(2, java.math.RoundingMode.HALF_UP));

            station.setStatus("ONLINE");
            station.setLastSeen(LocalDateTime.now());
            stationRepository.save(station);

            heartbeatRepository.save(heartbeat);

            eventService.addEvent(
                    station,
                    "ONLINE",
                    "Automatic heartbeat from station"
            );

            System.out.println("HEARTBEAT SIMULATOR: " + station.getName());
        }
    }
}
