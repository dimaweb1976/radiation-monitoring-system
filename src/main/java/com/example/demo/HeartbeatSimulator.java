package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Component
@Profile("demo")
public class HeartbeatSimulator {

    private final StationRepository stationRepository;
    private final HeartbeatService heartbeatService;
    private final Random random = new Random();

    public HeartbeatSimulator(
            StationRepository stationRepository,
            HeartbeatService heartbeatService
    ) {
        this.stationRepository = stationRepository;
        this.heartbeatService = heartbeatService;
    }

    @Scheduled(fixedRate = 30000)
    public void generateHeartbeat() {
        List<Station> stations = stationRepository.findAll();

        if (stations.isEmpty()) {
            System.out.println("HEARTBEAT SIMULATOR: no stations found");
            return;
        }

        for (Station station : stations) {
            double cpu = 40 + random.nextDouble() * 25;
            double disk = 10 + random.nextDouble() * 20;
            double ram = 30 + random.nextDouble() * 40;

            heartbeatService.record(station.getId(), new HeartbeatRequest(
                    java.util.UUID.randomUUID().toString(),
                    BigDecimal.valueOf(cpu).setScale(2, java.math.RoundingMode.HALF_UP),
                    BigDecimal.valueOf(disk).setScale(2, java.math.RoundingMode.HALF_UP),
                    BigDecimal.valueOf(ram).setScale(2, java.math.RoundingMode.HALF_UP)));

            System.out.println("HEARTBEAT SIMULATOR: " + station.getName());
        }
    }
}
