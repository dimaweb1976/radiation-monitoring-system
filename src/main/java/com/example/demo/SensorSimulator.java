package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Component
@Profile("demo")
public class SensorSimulator {

    private final MeasurementService measurementService;
    private final DetectorRepository detectorRepository;
    private final Random random = new Random();

    public SensorSimulator(
            MeasurementService measurementService,
            DetectorRepository detectorRepository
    ) {
        this.measurementService = measurementService;
        this.detectorRepository = detectorRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void generate() {
        List<Detector> detectors = detectorRepository.findAll();

        if (detectors.isEmpty()) {
            System.out.println("SIMULATOR: no detectors found");
            return;
        }

        Detector detector = detectors.get(random.nextInt(detectors.size()));

        double raw = random.nextDouble() * 1.4;
        BigDecimal value = BigDecimal.valueOf(raw).setScale(3, RoundingMode.HALF_UP);

        if (detector.getStation() == null) return;
        measurementService.record(detector.getId(), value, "uSv/h", java.util.UUID.randomUUID().toString(),
                java.time.Instant.now(), detector.getStation().getId());

        System.out.println("SIMULATOR: " + detector.getName() + " = " + value);
    }
}
